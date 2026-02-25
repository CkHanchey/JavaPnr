package com.pnrgov.core.services;

import com.pnrgov.core.models.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates Flight Manifest PNRGOV EDIFACT messages with multiple PNRs.
 * Uses SampleDataGenerator for realistic reservation data.
 * Ported from PnrGov.Core.Services.FlightManifestGenerator (C#).
 */
@Service
public class FlightManifestGenerator {

    private static final String SEGMENT_TERMINATOR = "'";
    private static final String DATA_ELEMENT_SEPARATOR = "+";
    private static final String COMPONENT_SEPARATOR = ":";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("ddMMyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("ddMMMyy");
    private static final DateTimeFormatter DEP_DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");

    private static final String[] AIRLINES = {"AA", "UA", "DL", "SW", "B6", "JB", "AS", "F9", "NK", "G4"};
    private static final String[] PORTS = {"JFK", "LAX", "ORD", "DEN", "BOS", "SFO", "ATL", "MIA", "LHR", "CDG"};

    private final SampleDataGenerator sampleDataGenerator;
    private final Random random = new Random();

    public FlightManifestGenerator(SampleDataGenerator sampleDataGenerator) {
        this.sampleDataGenerator = sampleDataGenerator;
    }

    public String generateFlightManifest(int pnrCount, String airline, String flightNumber, String receiver) {
        StringBuilder sb = new StringBuilder();
        String messageRefNumber = generateMessageReference();
        String interchangeRefNumber = generateInterchangeReference();

        if (airline == null || airline.isEmpty()) airline = AIRLINES[random.nextInt(AIRLINES.length)];
        if (receiver == null || receiver.isEmpty()) receiver = "USCBP";

        // Generate manifested flight details
        LocalDateTime departureDateTime = LocalDateTime.now().plusDays(random.nextInt(30) + 1);
        String departureTime = String.format("%02d%02d", random.nextInt(24), random.nextInt(60));
        String arrivalTime   = String.format("%02d%02d", random.nextInt(24), random.nextInt(60));

        String originPort = PORTS[random.nextInt(PORTS.length)];
        String destPort;
        do {
            destPort = PORTS[random.nextInt(PORTS.length)];
        } while (destPort.equals(originPort));

        if (flightNumber == null || flightNumber.isEmpty()) {
            flightNumber = String.format("%04d", random.nextInt(9900) + 100);
        }

        // Build reporting flight (shared across all PNRs)
        int depHour = Integer.parseInt(departureTime.substring(0, 2));
        int depMin  = Integer.parseInt(departureTime.substring(2, 4));
        int arrHour = Integer.parseInt(arrivalTime.substring(0, 2));
        int arrMin  = Integer.parseInt(arrivalTime.substring(2, 4));

        LocalDateTime reportingFlightDep = departureDateTime
                .withHour(depHour).withMinute(depMin).withSecond(0).withNano(0);
        LocalDateTime reportingFlightArr = departureDateTime
                .withHour(arrHour).withMinute(arrMin).withSecond(0).withNano(0);
        if (!reportingFlightArr.isAfter(reportingFlightDep)) {
            reportingFlightArr = reportingFlightArr.plusDays(1);
        }

        final String manifestAirline    = airline;
        final String manifestFlight     = flightNumber;
        final String manifestDestPort   = destPort;
        final String manifestOriginPort = originPort;

        Flight reportingFlight = Flight.builder()
                .flightNumber(manifestFlight)
                .airlineCode(manifestAirline)
                .departureAirport(manifestOriginPort)
                .arrivalAirport(manifestDestPort)
                .departureDate(reportingFlightDep)
                .arrivalDate(reportingFlightArr)
                .serviceClass("Y")
                .segmentNumber(1)
                .flightStatus("HK")
                .aircraftType("320")
                .build();

        // Generate multiple PNRs
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < pnrCount; i++) {
            int passengerCount = random.nextInt(4) + 1;
            boolean includeCodeshare = random.nextInt(3) == 0; // Less frequent
            boolean includeThruFlight = random.nextInt(3) == 0; // Less frequent
            boolean includeBags = random.nextInt(2) == 0;
            boolean includeSeats = random.nextInt(2) == 0;
            boolean includeDocuments = random.nextInt(2) == 0;
            boolean includePayment = random.nextInt(2) == 0;
            boolean includePhoneNumbers = random.nextInt(2) == 0;
            boolean includeAgencyInfo = random.nextInt(2) == 0;
            boolean includeCreditCard = includePayment && random.nextInt(2) == 0;

            // For manifests, generate only 1 flight which will be replaced by reporting flight
            Reservation reservation = sampleDataGenerator.generateRandomReservation(
                    passengerCount,
                    1,                  // flightCount: always 1, replaced by reporting flight
                    includeBags,
                    includeSeats,
                    includeDocuments,
                    includePayment,
                    includeCodeshare,
                    includeThruFlight,
                    includePhoneNumbers,
                    includeAgencyInfo,
                    includeCreditCard
            );

            // Replace the generated flight with the reporting flight and update seat associations
            Long oldFlightId = reservation.getFlights().isEmpty() ? null
                    : reservation.getFlights().get(0).getId();
            reservation.getFlights().clear();
            reservation.getFlights().add(reportingFlight);

            // Update all seat assignments to reference the reporting flight
            final Long oldFlightIdFinal = oldFlightId;
            for (Passenger pax : reservation.getPassengers()) {
                for (SeatAssignment seat : pax.getSeats()) {
                    if (oldFlightIdFinal != null && oldFlightIdFinal.equals(seat.getFlightId())) {
                        seat.setFlight(reportingFlight);
                    }
                }
            }

            reservations.add(reservation);
        }

        int totalPassengers = reservations.stream().mapToInt(r -> r.getPassengers().size()).sum();

        // UNA – Service string advice
        sb.append("UNA:+.?*'").append("\n");

        // UNB – Interchange Header
        sb.append(generateUNB(interchangeRefNumber, airline, receiver)).append("\n");

        // UNG – Functional group header
        sb.append(generateUNG(interchangeRefNumber, airline, receiver)).append("\n");

        // UNH – Message Header
        sb.append(generateUNH(messageRefNumber, airline, flightNumber, departureDateTime, originPort, destPort)).append("\n");

        // MSG – Message action details
        sb.append(generateMSG()).append("\n");

        // ORG – Originator
        sb.append(generateORG(airline)).append("\n");

        // TVL – Reporting (manifested) flight
        sb.append(generateTVLHeader(departureDateTime, departureTime, arrivalTime,
                originPort, destPort, airline, flightNumber)).append("\n");

        // EQN – Total passengers on the manifested flight
        sb.append("EQN").append(DATA_ELEMENT_SEPARATOR)
                .append(totalPassengers).append(SEGMENT_TERMINATOR).append("\n");

        // PNR records for each reservation
        for (Reservation reservation : reservations) {
            sb.append(generatePnrFromReservation(reservation, airline));
        }

        // Segment count (UNA is not counted in EDIFACT segment count)
        String[] allLines = sb.toString().split("[\r\n]+");
        long segmentCount = Arrays.stream(allLines)
                .filter(l -> !l.isEmpty() && !l.startsWith("UNA"))
                .count();

        // UNT – Message Trailer
        sb.append(generateUNT((int) segmentCount + 1, messageRefNumber)).append("\n");

        // UNE – Functional group trailer
        sb.append(generateUNE(interchangeRefNumber)).append("\n");

        // UNZ – Interchange Trailer
        sb.append(generateUNZ(interchangeRefNumber)).append("\n");

        return sb.toString();
    }

    // ── PNR section ──────────────────────────────────────────────────────────

    private String generatePnrFromReservation(Reservation reservation, String manifestAirline) {
        StringBuilder sb = new StringBuilder();

        // SRC – Start of PNR section
        sb.append("SRC").append(SEGMENT_TERMINATOR).append("\n");

        // RCI – Reservation control information
        String airlineCode = reservation.getFlights().isEmpty()
                ? manifestAirline
                : reservation.getFlights().get(0).getAirlineCode();
        sb.append(generateRCI(reservation, airlineCode)).append("\n");

        // DAT – Last PNR transaction date/time
        sb.append(generateTransactionDAT(reservation.getCreatedDate())).append("\n");

        // IFT – OSI free text contact details
        if (reservation.getContactPhone() != null && !reservation.getContactPhone().isEmpty()) {
            sb.append(generateIFT(airlineCode + " PHONE " + reservation.getContactPhone().toUpperCase())).append("\n");
        }
        if (reservation.getContactEmail() != null && !reservation.getContactEmail().isEmpty()) {
            sb.append(generateIFT(airlineCode + " EMAIL " + reservation.getContactEmail().toUpperCase())).append("\n");
        }

        // ORG – Booking agent
        String agencyCode = (reservation.getAgencyCode() != null && !reservation.getAgencyCode().isEmpty())
                ? reservation.getAgencyCode()
                : "TTY";
        sb.append(generateBookingORG(agencyCode)).append("\n");

        // Per-passenger segments
        int passengerIndex = 1;
        for (Passenger passenger : reservation.getPassengers()) {
            // TIF – Traveller information
            sb.append(generateTIF(passenger, passengerIndex)).append("\n");

            // SSR DOCS – Passport information
            for (TravelDocument document : passenger.getDocuments()) {
                sb.append(generateSSR_DOCS(document, passenger, passengerIndex)).append("\n");
            }

            // SSR TKNE – Ticket numbers for each flight
            List<Flight> sortedFlights = reservation.getFlights().stream()
                    .sorted(Comparator.comparingInt(f -> f.getSegmentNumber() != null ? f.getSegmentNumber() : 0))
                    .collect(Collectors.toList());
            int flightIndex = 1;
            for (Flight flight : sortedFlights) {
                sb.append(generateSSR_TKNE(flight, passengerIndex, flightIndex)).append("\n");
                flightIndex++;
            }

            passengerIndex++;
        }

        // TVL + RPI + APD + SSR SEAT + SSR TKNE + RCI per flight
        List<Flight> sortedFlightsForTVL = reservation.getFlights().stream()
                .sorted(Comparator.comparingInt(f -> f.getSegmentNumber() != null ? f.getSegmentNumber() : 0))
                .collect(Collectors.toList());

        // Build passenger ID to index map for proper seat ordering
        Map<Long, Integer> passengerIdToIndex = new HashMap<>();
        int pIdx = 1;
        for (Passenger p : reservation.getPassengers()) {
            if (p.getId() != null) passengerIdToIndex.put(p.getId(), pIdx);
            pIdx++;
        }

        for (Flight flight : sortedFlightsForTVL) {
            // TVL – Flight segment
            sb.append(generateTVLFlight(flight)).append("\n");

            // RPI – Reporting flight information
            String status = flight.getFlightStatus() != null ? flight.getFlightStatus() : "HK";
            sb.append(generateRPI(reservation.getPassengers().size(), status)).append("\n");

            // APD – Equipment type
            String aircraftType = flight.getAircraftType() != null ? flight.getAircraftType() : "320";
            sb.append("APD").append(DATA_ELEMENT_SEPARATOR).append(aircraftType)
                    .append(SEGMENT_TERMINATOR).append("\n");

            // SSR SEAT – Seat assignments for this flight
            final Flight currentFlight = flight;
            List<SeatAssignment> seatsForFlight = reservation.getPassengers().stream()
                    .flatMap(p -> p.getSeats().stream())
                    .filter(s -> {
                        if (currentFlight.getId() != null)
                            return currentFlight.getId().equals(s.getFlightId());
                        return s.getFlight() == currentFlight; // unpersisted flight: use reference equality
                    })
                    .sorted(Comparator.comparingInt(s -> passengerIdToIndex.getOrDefault(s.getPassengerId(), 0)))
                    .collect(Collectors.toList());

            if (!seatsForFlight.isEmpty()) {
                StringBuilder seatListBuilder = new StringBuilder();
                for (SeatAssignment seat : seatsForFlight) {
                    int seatPaxIdx = passengerIdToIndex.getOrDefault(seat.getPassengerId(), 1);
                    seatListBuilder.append(DATA_ELEMENT_SEPARATOR)
                            .append(seat.getSeatNumber())
                            .append(COMPONENT_SEPARATOR).append(COMPONENT_SEPARATOR)
                            .append(seatPaxIdx);
                }
                sb.append(generateSSR_SEAT(flight, reservation.getPassengers().size(), seatListBuilder.toString())).append("\n");
            }

            // SSR TKNE – Ticket number for each passenger on this flight
            int ssrPaxIndex = 1;
            for (Passenger passenger : reservation.getPassengers()) {
                sb.append(generateSSR_TKNE_Simple(flight, ssrPaxIndex)).append("\n");
                ssrPaxIndex++;
            }

            // RCI – Record locator after each flight
            sb.append(generateRCI(reservation, airlineCode)).append("\n");
        }

        return sb.toString();
    }

    // ── Interchange / envelope segments ─────────────────────────────────────

    private String generateUNB(String interchangeRef, String sender, String receiver) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("ddMMyy"));
        String time = now.format(TIME_FMT);
        return "UNB" + DATA_ELEMENT_SEPARATOR + "IATA" + COMPONENT_SEPARATOR + "1"
                + DATA_ELEMENT_SEPARATOR + sender
                + DATA_ELEMENT_SEPARATOR + receiver
                + DATA_ELEMENT_SEPARATOR + date + COMPONENT_SEPARATOR + time
                + DATA_ELEMENT_SEPARATOR + interchangeRef
                + DATA_ELEMENT_SEPARATOR + "PNRGOV" + SEGMENT_TERMINATOR;
    }

    private String generateUNG(String groupRef, String sender, String receiver) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("ddMMyy"));
        String time = now.format(TIME_FMT);
        return "UNG" + DATA_ELEMENT_SEPARATOR + "PNRGOV"
                + DATA_ELEMENT_SEPARATOR + sender
                + DATA_ELEMENT_SEPARATOR + receiver
                + DATA_ELEMENT_SEPARATOR + date + COMPONENT_SEPARATOR + time
                + DATA_ELEMENT_SEPARATOR + groupRef
                + DATA_ELEMENT_SEPARATOR + "IA"
                + DATA_ELEMENT_SEPARATOR + "11" + COMPONENT_SEPARATOR + "1" + SEGMENT_TERMINATOR;
    }

    private String generateUNH(String messageRef, String airline, String flightNumber,
                                LocalDateTime departureDate, String origin, String destination) {
        // Pad flight number left to 4 chars with zeros
        String paddedFlight = flightNumber.length() >= 4 ? flightNumber
                : "0000".substring(0, 4 - flightNumber.length()) + flightNumber;
        String flightInfo = airline + paddedFlight + departureDate.format(DEP_DATE_FMT) + origin + destination + "001";
        return "UNH" + DATA_ELEMENT_SEPARATOR + messageRef
                + DATA_ELEMENT_SEPARATOR + "PNRGOV" + COMPONENT_SEPARATOR + "11"
                + COMPONENT_SEPARATOR + "1" + COMPONENT_SEPARATOR + "IA"
                + DATA_ELEMENT_SEPARATOR + flightInfo + SEGMENT_TERMINATOR;
    }

    private String generateMSG() {
        return "MSG" + DATA_ELEMENT_SEPARATOR + COMPONENT_SEPARATOR + "22" + SEGMENT_TERMINATOR;
    }

    private String generateORG(String airlineCode) {
        return "ORG" + DATA_ELEMENT_SEPARATOR + airlineCode + SEGMENT_TERMINATOR;
    }

    private String generateBookingORG(String agencyCode) {
        return "ORG" + DATA_ELEMENT_SEPARATOR + "XX" + COMPONENT_SEPARATOR + agencyCode + SEGMENT_TERMINATOR;
    }

    // ── TVL segments ─────────────────────────────────────────────────────────

    /** Manifest-level TVL (header reporting flight). */
    private String generateTVLHeader(LocalDateTime departureDate, String departureTime,
                                     String arrivalTime, String origin, String destination,
                                     String airline, String flightNumber) {
        String depDate = departureDate.format(DATE_FMT);
        String arrDate = departureDate.format(DATE_FMT);
        return "TVL" + DATA_ELEMENT_SEPARATOR + depDate + COMPONENT_SEPARATOR + departureTime
                + COMPONENT_SEPARATOR + arrDate + COMPONENT_SEPARATOR + arrivalTime
                + DATA_ELEMENT_SEPARATOR + origin
                + DATA_ELEMENT_SEPARATOR + destination
                + DATA_ELEMENT_SEPARATOR + airline
                + DATA_ELEMENT_SEPARATOR + flightNumber + COMPONENT_SEPARATOR + "Y" + SEGMENT_TERMINATOR;
    }

    /** Per-PNR flight TVL segment. */
    private String generateTVLFlight(Flight flight) {
        String depDate = flight.getDepartureDate().format(DATE_FMT);
        String depTime = flight.getDepartureDate().format(TIME_FMT);
        String arrDate = flight.getArrivalDate().format(DATE_FMT);
        String arrTime = flight.getArrivalDate().format(TIME_FMT);
        String sc = flight.getServiceClass() != null ? flight.getServiceClass() : "Y";
        return "TVL" + DATA_ELEMENT_SEPARATOR + depDate + COMPONENT_SEPARATOR + depTime
                + COMPONENT_SEPARATOR + arrDate + COMPONENT_SEPARATOR + arrTime
                + DATA_ELEMENT_SEPARATOR + flight.getDepartureAirport()
                + DATA_ELEMENT_SEPARATOR + flight.getArrivalAirport()
                + DATA_ELEMENT_SEPARATOR + flight.getAirlineCode()
                + DATA_ELEMENT_SEPARATOR + flight.getFlightNumber() + COMPONENT_SEPARATOR + sc
                + SEGMENT_TERMINATOR;
    }

    // ── Passenger segments ───────────────────────────────────────────────────

    private String generateTIF(Passenger passenger, int passengerIndex) {
        String paxType = "ADT".equals(passenger.getPassengerType()) ? "A"
                : (passenger.getPassengerType() != null ? passenger.getPassengerType() : "A");
        String title = passenger.getTitle() != null ? passenger.getTitle() : "";
        return "TIF" + DATA_ELEMENT_SEPARATOR + passenger.getLastName().toUpperCase()
                + DATA_ELEMENT_SEPARATOR + passenger.getFirstName().toUpperCase() + " " + title
                + COMPONENT_SEPARATOR + paxType
                + COMPONENT_SEPARATOR + passengerIndex + ".1" + SEGMENT_TERMINATOR;
    }

    private String generateRPI(int passengerCount, String status) {
        return "RPI" + DATA_ELEMENT_SEPARATOR + passengerCount
                + DATA_ELEMENT_SEPARATOR + status + SEGMENT_TERMINATOR;
    }

    private String generateIFT(String text) {
        return "IFT" + DATA_ELEMENT_SEPARATOR + "4" + COMPONENT_SEPARATOR + "28"
                + DATA_ELEMENT_SEPARATOR + text + SEGMENT_TERMINATOR;
    }

    private String generateRCI(Reservation reservation, String airline) {
        String date = reservation.getCreatedDate().format(DATE_FMT);
        String time = reservation.getCreatedDate().format(TIME_FMT);
        return "RCI" + DATA_ELEMENT_SEPARATOR + airline
                + COMPONENT_SEPARATOR + reservation.getRecordLocator()
                + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + date + COMPONENT_SEPARATOR + time
                + SEGMENT_TERMINATOR;
    }

    private String generateTransactionDAT(LocalDateTime transactionDate) {
        String date = transactionDate.format(DATE_FMT);
        String time = transactionDate.format(TIME_FMT);
        return "DAT" + DATA_ELEMENT_SEPARATOR + "700"
                + COMPONENT_SEPARATOR + date + COMPONENT_SEPARATOR + time + SEGMENT_TERMINATOR;
    }

    private String generateSSR_DOCS(TravelDocument document, Passenger passenger, int passengerIndex) {
        String dob = passenger.getDateOfBirth() != null
                ? passenger.getDateOfBirth().format(DOB_FMT).toUpperCase()
                : "01JAN80";
        String expiry = document.getExpiryDate() != null
                ? document.getExpiryDate().format(DOB_FMT).toUpperCase()
                : "01JAN30";
        String gender          = passenger.getGender() != null ? passenger.getGender() : "U";
        String nationality     = document.getNationality() != null ? document.getNationality() : "US";
        String issuingCountry  = document.getIssuingCountry() != null ? document.getIssuingCountry() : "US";

        return "SSR" + DATA_ELEMENT_SEPARATOR + "DOCS" + COMPONENT_SEPARATOR + "HK"
                + COMPONENT_SEPARATOR + "1"
                + COMPONENT_SEPARATOR + issuingCountry
                + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR
                + COMPONENT_SEPARATOR + "/P/" + nationality + "/" + document.getDocumentNumber()
                + "/" + issuingCountry + "/" + dob + "/" + gender + "/" + expiry
                + "/" + passenger.getLastName().toUpperCase() + "/" + passenger.getFirstName().toUpperCase()
                + DATA_ELEMENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + passengerIndex + ".1"
                + SEGMENT_TERMINATOR;
    }

    private String generateSSR_TKNE(Flight flight, int passengerIndex, int flightIndex) {
        String ticketNumber = "139" + (random.nextInt(9000000) + 1000000) + "000C" + flightIndex;
        return "SSR" + DATA_ELEMENT_SEPARATOR + "TKNE" + COMPONENT_SEPARATOR + "HK"
                + COMPONENT_SEPARATOR + "1"
                + COMPONENT_SEPARATOR + flight.getAirlineCode()
                + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR
                + flight.getDepartureAirport() + COMPONENT_SEPARATOR + flight.getArrivalAirport()
                + COMPONENT_SEPARATOR + ticketNumber
                + DATA_ELEMENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + passengerIndex + ".1"
                + SEGMENT_TERMINATOR;
    }

    private String generateSSR_TKNE_Simple(Flight flight, int passengerIndex) {
        String ticketNumber = "139" + (random.nextInt(9000000) + 1000000) + "000C1";
        return "SSR" + DATA_ELEMENT_SEPARATOR + "TKNE" + COMPONENT_SEPARATOR + "HK"
                + COMPONENT_SEPARATOR + "1"
                + COMPONENT_SEPARATOR + flight.getAirlineCode()
                + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR
                + flight.getDepartureAirport() + COMPONENT_SEPARATOR + flight.getArrivalAirport()
                + COMPONENT_SEPARATOR + "." + ticketNumber + SEGMENT_TERMINATOR;
    }

    private String generateSSR_SEAT(Flight flight, int passengerCount, String seatList) {
        return "SSR" + DATA_ELEMENT_SEPARATOR + "SEAT" + COMPONENT_SEPARATOR + "HK"
                + COMPONENT_SEPARATOR + passengerCount
                + COMPONENT_SEPARATOR + flight.getAirlineCode()
                + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR
                + flight.getDepartureAirport() + COMPONENT_SEPARATOR + flight.getArrivalAirport()
                + seatList + SEGMENT_TERMINATOR;
    }

    // ── Trailer segments ─────────────────────────────────────────────────────

    private String generateUNT(int segmentCount, String messageRef) {
        return "UNT" + DATA_ELEMENT_SEPARATOR + segmentCount
                + DATA_ELEMENT_SEPARATOR + messageRef + SEGMENT_TERMINATOR;
    }

    private String generateUNE(String groupRef) {
        return "UNE" + DATA_ELEMENT_SEPARATOR + "1"
                + DATA_ELEMENT_SEPARATOR + groupRef + SEGMENT_TERMINATOR;
    }

    private String generateUNZ(String interchangeRef) {
        return "UNZ" + DATA_ELEMENT_SEPARATOR + "1"
                + DATA_ELEMENT_SEPARATOR + interchangeRef + SEGMENT_TERMINATOR;
    }

    // ── References ───────────────────────────────────────────────────────────

    private String generateMessageReference() {
        return String.format("%06d", random.nextInt(999999) + 1);
    }

    private String generateInterchangeReference() {
        return String.valueOf(1000000000000L + (long) (random.nextDouble() * 8999999999999L));
    }
}
