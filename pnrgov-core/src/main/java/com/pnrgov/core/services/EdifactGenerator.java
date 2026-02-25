package com.pnrgov.core.services;

import com.pnrgov.core.models.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates PNRGOV EDIFACT messages according to version 21.1
 */
@Service
public class EdifactGenerator {
    
    private static final String SEGMENT_TERMINATOR = "'";
    private static final String DATA_ELEMENT_SEPARATOR = "+";
    private static final String COMPONENT_SEPARATOR = ":";
    private final Random random = new Random();
    
    public String generatePnrGov(Reservation reservation, String receiver) {
        StringBuilder sb = new StringBuilder();
        String messageRefNumber = generateMessageReference();
        String interchangeRefNumber = generateInterchangeReference();
        
        // Default sender to reporting flight carrier code
        Flight reportingFlight = reservation.getFlights().stream()
            .sorted((f1, f2) -> Integer.compare(f1.getSegmentNumber(), f2.getSegmentNumber()))
            .findFirst().orElse(null);
        String sender = reportingFlight != null ? reportingFlight.getAirlineCode() : "XX";
        
        // Default receiver to USCBP if not provided
        receiver = receiver != null ? receiver : "USCBP";
        
        // UNA - Service string advice (character set definition)
        sb.append("UNA:+.?*'\n");
        
        // UNB - Interchange Header
        sb.append(generateUNB(interchangeRefNumber, sender, receiver)).append("\n");
        
        // UNG - Functional group header
        sb.append(generateUNG(interchangeRefNumber, sender, receiver)).append("\n");
        
        // UNH - Message Header
        sb.append(generateUNH(messageRefNumber, reservation)).append("\n");
        
        // MSG - Message action details
        sb.append(generateMSG()).append("\n");
        
        // ORG - Originator of request
        String airlineCode = reservation.getFlights().isEmpty() ? "XX" : 
            reservation.getFlights().get(0).getAirlineCode();
        sb.append(generateORG(airlineCode)).append("\n");
        
        // TVL - Reporting flight (the flight being reported on) - Level 0 uses operating carrier only
        if (reportingFlight != null) {
            sb.append(generateHeaderTVL(reportingFlight)).append("\n");
        }
        
        // EQN - Total number of PNRs
        sb.append("EQN").append(DATA_ELEMENT_SEPARATOR).append(reservation.getPassengers().size())
            .append(SEGMENT_TERMINATOR).append("\n");
        
        // SRC' - Start of PNR section
        sb.append("SRC").append(SEGMENT_TERMINATOR).append("\n");
        
        // RCI - Reservation control information with date/time
        sb.append(generateRCI(reservation)).append("\n");
        
        // DAT - Ticket issue / last PNR transaction date/time
        sb.append(generateTransactionDAT(reservation.getCreatedDate())).append("\n");
        
        // IFT - OSI free text information (contact details)
        if (reservation.getContactPhone() != null && !reservation.getContactPhone().isEmpty()) {
            sb.append(generateIFT(airlineCode + " " + reservation.getContactPhone().toUpperCase())).append("\n");
        }
        if (reservation.getContactEmail() != null && !reservation.getContactEmail().isEmpty()) {
            sb.append(generateIFT(airlineCode + " " + reservation.getContactEmail().toUpperCase())).append("\n");
        }
        
        // ORG - Booking agent
        sb.append(generateBookingORG(reservation.getAgencyCode() != null ? reservation.getAgencyCode() : "TTY"))
            .append("\n");
        
        // Loop for each passenger
        int passengerIndex = 1;
        for (Passenger passenger : reservation.getPassengers()) {
            // TIF - Traveller information with passenger reference
            sb.append(generateTIF(passenger, passengerIndex)).append("\n");
            
            // SSR - DOCS for passport information
            for (TravelDocument document : passenger.getDocuments()) {
                sb.append(generateSSR_DOCS(document, passenger, passengerIndex)).append("\n");
            }
            
            // SSR - TKNE for tickets (for each flight)
            int flightIndex = 1;
            for (Flight flight : reservation.getFlights().stream()
                    .sorted((f1, f2) -> Integer.compare(f1.getSegmentNumber(), f2.getSegmentNumber()))
                    .collect(Collectors.toList())) {
                sb.append(generateSSR_TKNE(flight, passengerIndex, flightIndex)).append("\n");
                flightIndex++;
            }
            
            passengerIndex++;
        }
        
        // TVL segments for each flight with associated data
        for (Flight flight : reservation.getFlights().stream()
                .sorted((f1, f2) -> Integer.compare(f1.getSegmentNumber(), f2.getSegmentNumber()))
                .collect(Collectors.toList())) {
            // TVL - Travel product information
            sb.append(generateTVL(flight)).append("\n");
            
            // TRA - Transport details (for codeshare flights)
            if (isCodeshare(flight)) {
                sb.append(generateTRA(flight)).append("\n");
            }
            
            // RPI - Reporting flight information (passenger count and status)
            sb.append(generateRPI(reservation.getPassengers().size(), flight.getFlightStatus())).append("\n");
            
            // APD - Equipment Type (aircraft)
            sb.append("APD").append(DATA_ELEMENT_SEPARATOR).append(flight.getAircraftType())
                .append(SEGMENT_TERMINATOR).append("\n");
            
            // SSR - SEAT information for all passengers on this flight
            Map<Long, Integer> passengerIdToIndex = new HashMap<>();
            int paxIdx = 1;
            for (Passenger pax : reservation.getPassengers()) {
                if (pax.getId() != null) passengerIdToIndex.put(pax.getId(), paxIdx);
                paxIdx++;
            }
            Long fid = flight.getId();
            List<SeatAssignment> seatsForFlight = reservation.getPassengers().stream()
                .flatMap(p -> p.getSeats().stream())
                .filter(s -> fid != null && fid.equals(s.getFlightId()))
                .sorted(Comparator.comparingInt(s -> passengerIdToIndex.getOrDefault(s.getPassengerId(), 0)))
                .collect(Collectors.toList());

            if (!seatsForFlight.isEmpty()) {
                StringBuilder seatListBuilder = new StringBuilder();
                for (SeatAssignment seat : seatsForFlight) {
                    int idx = passengerIdToIndex.getOrDefault(seat.getPassengerId(), 1);
                    seatListBuilder.append(DATA_ELEMENT_SEPARATOR)
                        .append(seat.getSeatNumber())
                        .append(COMPONENT_SEPARATOR).append(COMPONENT_SEPARATOR)
                        .append(idx);
                }
                sb.append(generateSSR_SEAT(flight, reservation.getPassengers().size(), seatListBuilder.toString())).append("\n");
            }
            
            // SSR - TKNE for tickets on this flight
            int paxIndex = 1;
            for (Passenger passenger : reservation.getPassengers()) {
                sb.append(generateSSR_TKNE_Simple(flight, paxIndex)).append("\n");
                paxIndex++;
            }
            
            // RCI - Record locator after each flight
            sb.append(generateRCI(reservation)).append("\n");
        }
        
        // Count segments (excluding UNA which is not counted)
        String[] lines = sb.toString().split("\\r?\\n");
        long segmentCount = java.util.Arrays.stream(lines)
            .filter(l -> !l.startsWith("UNA"))
            .count();
        
        // UNT - Message Trailer
        sb.append(generateUNT((int)segmentCount + 1, messageRefNumber)).append("\n");
        
        // UNE - Functional group trailer
        sb.append(generateUNE(interchangeRefNumber)).append("\n");
        
        // UNZ - Interchange Trailer
        sb.append(generateUNZ(interchangeRefNumber)).append("\n");
        
        return sb.toString();
    }
    
    private String generateUNB(String interchangeRef, String sender, String receiver) {
        LocalDateTime dateTime = LocalDateTime.now();
        String date = dateTime.format(DateTimeFormatter.ofPattern("ddMMyy"));
        String time = dateTime.format(DateTimeFormatter.ofPattern("HHmm"));
        
        return "UNB" + DATA_ELEMENT_SEPARATOR + "IATA" + COMPONENT_SEPARATOR + "1" +
               DATA_ELEMENT_SEPARATOR + sender +
               DATA_ELEMENT_SEPARATOR + receiver +
               DATA_ELEMENT_SEPARATOR + date + COMPONENT_SEPARATOR + time +
               DATA_ELEMENT_SEPARATOR + interchangeRef +
               DATA_ELEMENT_SEPARATOR + "PNRGOV" + SEGMENT_TERMINATOR;
    }
    
    private String generateUNG(String groupRef, String sender, String receiver) {
        LocalDateTime dateTime = LocalDateTime.now();
        String date = dateTime.format(DateTimeFormatter.ofPattern("ddMMyy"));
        String time = dateTime.format(DateTimeFormatter.ofPattern("HHmm"));
        
        return "UNG" + DATA_ELEMENT_SEPARATOR + "PNRGOV" +
               DATA_ELEMENT_SEPARATOR + sender +
               DATA_ELEMENT_SEPARATOR + receiver +
               DATA_ELEMENT_SEPARATOR + date + COMPONENT_SEPARATOR + time +
               DATA_ELEMENT_SEPARATOR + groupRef +
               DATA_ELEMENT_SEPARATOR + "IA" +
               DATA_ELEMENT_SEPARATOR + "11" + COMPONENT_SEPARATOR + "1" + SEGMENT_TERMINATOR;
    }
    
    private String generateUNH(String messageRef, Reservation reservation) {
        Flight flight = reservation.getFlights().isEmpty() ? null : reservation.getFlights().get(0);
        String flightInfo;
        if (flight != null) {
            String depDate = flight.getDepartureDate().format(DateTimeFormatter.ofPattern("ddMMyy"));
            String depTime = flight.getDepartureDate().format(DateTimeFormatter.ofPattern("HHmm"));
            flightInfo = flight.getAirlineCode() + flight.getFlightNumber() + "/" + depDate + "/" + depTime;
        } else {
            flightInfo = "XXXX/000000/0000";
        }
            
        return "UNH" + DATA_ELEMENT_SEPARATOR + messageRef +
               DATA_ELEMENT_SEPARATOR + "PNRGOV" + COMPONENT_SEPARATOR + "11" + COMPONENT_SEPARATOR + "1" + 
               COMPONENT_SEPARATOR + "IA" +
               DATA_ELEMENT_SEPARATOR + flightInfo + SEGMENT_TERMINATOR;
    }
    
    private String generateMSG() {
        return "MSG" + DATA_ELEMENT_SEPARATOR + COMPONENT_SEPARATOR + "22" + SEGMENT_TERMINATOR;
    }
    
    private String generateORG(String airlineCode) {
        return "ORG" + DATA_ELEMENT_SEPARATOR + airlineCode + SEGMENT_TERMINATOR;
    }
    
    private String generateBookingORG(String agencyCode) {
        String airline = "XX";
        return "ORG" + DATA_ELEMENT_SEPARATOR + airline + COMPONENT_SEPARATOR + agencyCode + SEGMENT_TERMINATOR;
    }
    
    /**
     * Generates the Level 0 (header) TVL segment for the reporting flight.
     * Per PNRGOV spec 5.29.1: C306 contains the OPERATING airline designator only.
     * No reservation booking designator (RBD/service class) in this form.
     */
    private String generateHeaderTVL(Flight flight) {
        String depDate = flight.getDepartureDate().format(DateTimeFormatter.ofPattern("ddMMyy"));
        String depTime = flight.getDepartureDate().format(DateTimeFormatter.ofPattern("HHmm"));
        String arrDate = flight.getArrivalDate().format(DateTimeFormatter.ofPattern("ddMMyy"));
        String arrTime = flight.getArrivalDate().format(DateTimeFormatter.ofPattern("HHmm"));

        // Level 0 TVL always contains the operating carrier code (C306:1), never the marketing carrier
        String operatingCode = (flight.getOperatingCarrier() != null && !flight.getOperatingCarrier().isEmpty())
            ? flight.getOperatingCarrier()
            : flight.getAirlineCode();

        // Use the operating carrier's flight number if available, otherwise fall back to marketing flight number
        String opFlightNum = (flight.getOperatingFlightNumber() != null && !flight.getOperatingFlightNumber().isEmpty())
            ? flight.getOperatingFlightNumber()
            : flight.getFlightNumber();

        return "TVL" + DATA_ELEMENT_SEPARATOR + depDate + COMPONENT_SEPARATOR + depTime + COMPONENT_SEPARATOR +
               arrDate + COMPONENT_SEPARATOR + arrTime +
               DATA_ELEMENT_SEPARATOR + flight.getDepartureAirport() +
               DATA_ELEMENT_SEPARATOR + flight.getArrivalAirport() +
               DATA_ELEMENT_SEPARATOR + operatingCode +
               DATA_ELEMENT_SEPARATOR + opFlightNum + COMPONENT_SEPARATOR + flight.getServiceClass() + SEGMENT_TERMINATOR;
    }

    /**
     * Generates a GR.5 (flight itinerary) TVL segment.
     * Per PNRGOV spec 5.29.2: C306:1 = marketing airline, C306:2 = operating airline (when different).
     * Includes the reservation booking designator (service class).
     * Per spec 5.29.3: Use TRA segment for operating carrier details; C306:2 is still populated here.
     */
    private String generateTVL(Flight flight) {
        String depDate = flight.getDepartureDate().format(DateTimeFormatter.ofPattern("ddMMyy"));
        String depTime = flight.getDepartureDate().format(DateTimeFormatter.ofPattern("HHmm"));
        String arrDate = flight.getArrivalDate().format(DateTimeFormatter.ofPattern("ddMMyy"));
        String arrTime = flight.getArrivalDate().format(DateTimeFormatter.ofPattern("HHmm"));

        // GR.5 TVL: marketing carrier in C306:1; operating carrier in C306:2 for codeshare flights
        String carrierCode = isCodeshare(flight)
            ? flight.getAirlineCode() + COMPONENT_SEPARATOR + flight.getOperatingCarrier()
            : flight.getAirlineCode();

        return "TVL" + DATA_ELEMENT_SEPARATOR + depDate + COMPONENT_SEPARATOR + depTime + COMPONENT_SEPARATOR +
               arrDate + COMPONENT_SEPARATOR + arrTime +
               DATA_ELEMENT_SEPARATOR + flight.getDepartureAirport() +
               DATA_ELEMENT_SEPARATOR + flight.getArrivalAirport() +
               DATA_ELEMENT_SEPARATOR + carrierCode +
               DATA_ELEMENT_SEPARATOR + flight.getFlightNumber() + COMPONENT_SEPARATOR +
               flight.getServiceClass() + SEGMENT_TERMINATOR;
    }
    
    private boolean isCodeshare(Flight flight) {
        return flight.getOperatingCarrier() != null && !flight.getOperatingCarrier().isEmpty() && 
               !flight.getAirlineCode().equals(flight.getOperatingCarrier());
    }
    
    private String generateTRA(Flight flight) {
        // Use the operating carrier's own flight number if stored, otherwise fall back to the marketing flight number
        String opFlightNum = (flight.getOperatingFlightNumber() != null && !flight.getOperatingFlightNumber().isEmpty())
            ? flight.getOperatingFlightNumber()
            : flight.getFlightNumber();
        return "TRA" + DATA_ELEMENT_SEPARATOR + flight.getOperatingCarrier() +
               DATA_ELEMENT_SEPARATOR + opFlightNum + COMPONENT_SEPARATOR + "D" + SEGMENT_TERMINATOR;
    }
    
    private String generateTIF(Passenger passenger, int passengerIndex) {
        String paxType = "ADT".equals(passenger.getPassengerType()) ? "A" : passenger.getPassengerType();
        
        return "TIF" + DATA_ELEMENT_SEPARATOR + passenger.getLastName().toUpperCase() +
               DATA_ELEMENT_SEPARATOR + passenger.getFirstName().toUpperCase() + " " + passenger.getTitle() +
               COMPONENT_SEPARATOR + paxType +
               COMPONENT_SEPARATOR + passengerIndex + ".1" + SEGMENT_TERMINATOR;
    }
    
    private String generateRPI(int passengerCount, String status) {
        return "RPI" + DATA_ELEMENT_SEPARATOR + passengerCount +
               DATA_ELEMENT_SEPARATOR + status + SEGMENT_TERMINATOR;
    }
    
    private String generateIFT(String text) {
        return "IFT" + DATA_ELEMENT_SEPARATOR + "4" + COMPONENT_SEPARATOR + "28" +
               DATA_ELEMENT_SEPARATOR + text + SEGMENT_TERMINATOR;
    }
    
    private String generateRCI(Reservation reservation) {
        String airline = reservation.getFlights().isEmpty() ? "XX" : 
            reservation.getFlights().get(0).getAirlineCode();
        String date = reservation.getCreatedDate().format(DateTimeFormatter.ofPattern("ddMMyy"));
        String time = reservation.getCreatedDate().format(DateTimeFormatter.ofPattern("HHmm"));
        
        return "RCI" + DATA_ELEMENT_SEPARATOR + airline + COMPONENT_SEPARATOR + reservation.getRecordLocator() +
               COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + date + COMPONENT_SEPARATOR + time + SEGMENT_TERMINATOR;
    }
    
    private String generateTransactionDAT(LocalDateTime transactionDate) {
        String date = transactionDate.format(DateTimeFormatter.ofPattern("ddMMyy"));
        String time = transactionDate.format(DateTimeFormatter.ofPattern("HHmm"));
        
        return "DAT" + DATA_ELEMENT_SEPARATOR + "700" +
               COMPONENT_SEPARATOR + date +
               COMPONENT_SEPARATOR + time + SEGMENT_TERMINATOR;
    }
    
    private String generateSSR_TKNE(Flight flight, int passengerIndex, int flightIndex) {
        String ticketNumber = "139" + (1000000 + random.nextInt(9000000)) + "000C" + flightIndex;
        
        return "SSR" + DATA_ELEMENT_SEPARATOR + "TKNE" + COMPONENT_SEPARATOR + "HK" + COMPONENT_SEPARATOR + "1" +
               COMPONENT_SEPARATOR + flight.getAirlineCode() +
               COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR +
               flight.getDepartureAirport() + COMPONENT_SEPARATOR + flight.getArrivalAirport() +
               COMPONENT_SEPARATOR + ticketNumber +
               DATA_ELEMENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + passengerIndex + ".1" + 
               SEGMENT_TERMINATOR;
    }
    
    private String generateSSR_TKNE_Simple(Flight flight, int passengerIndex) {
        String ticketNumber = "139" + (1000000 + random.nextInt(9000000)) + "000C1";
        
        return "SSR" + DATA_ELEMENT_SEPARATOR + "TKNE" + COMPONENT_SEPARATOR + "HK" + COMPONENT_SEPARATOR + "1" +
               COMPONENT_SEPARATOR + flight.getAirlineCode() +
               COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR +
               flight.getDepartureAirport() + COMPONENT_SEPARATOR + flight.getArrivalAirport() +
               COMPONENT_SEPARATOR + "." + ticketNumber + SEGMENT_TERMINATOR;
    }
    
    private String generateSSR_DOCS(TravelDocument document, Passenger passenger, int passengerIndex) {
        String dob = passenger.getDateOfBirth().format(DateTimeFormatter.ofPattern("ddMMMyy")).toUpperCase();
        String expiry = document.getExpiryDate().format(DateTimeFormatter.ofPattern("ddMMMyy")).toUpperCase();
        
        return "SSR" + DATA_ELEMENT_SEPARATOR + "DOCS" + COMPONENT_SEPARATOR + "HK" + COMPONENT_SEPARATOR + "1" +
               COMPONENT_SEPARATOR + document.getIssuingCountry() +
               COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR +
               COMPONENT_SEPARATOR + "/P/" + document.getNationality() + "/" + document.getDocumentNumber() +
               "/" + document.getIssuingCountry() + "/" + dob + "/" + passenger.getGender() + "/" + expiry +
               "/" + passenger.getLastName().toUpperCase() + "/" + passenger.getFirstName().toUpperCase() +
               DATA_ELEMENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + passengerIndex + ".1" + 
               SEGMENT_TERMINATOR;
    }
    
    private String generateSSR_SEAT(Flight flight, int passengerCount, String seatList) {
        return "SSR" + DATA_ELEMENT_SEPARATOR + "SEAT" + COMPONENT_SEPARATOR + "HK" + COMPONENT_SEPARATOR +
               passengerCount +
               COMPONENT_SEPARATOR + flight.getAirlineCode() +
               COMPONENT_SEPARATOR + COMPONENT_SEPARATOR + COMPONENT_SEPARATOR +
               flight.getDepartureAirport() + COMPONENT_SEPARATOR + flight.getArrivalAirport() +
               seatList + SEGMENT_TERMINATOR;
    }
    
    private String generateUNT(int segmentCount, String messageRef) {
        return "UNT" + DATA_ELEMENT_SEPARATOR + segmentCount +
               DATA_ELEMENT_SEPARATOR + messageRef + SEGMENT_TERMINATOR;
    }
    
    private String generateUNE(String groupRef) {
        return "UNE" + DATA_ELEMENT_SEPARATOR + "1" +
               DATA_ELEMENT_SEPARATOR + groupRef + SEGMENT_TERMINATOR;
    }
    
    private String generateUNZ(String interchangeRef) {
        return "UNZ" + DATA_ELEMENT_SEPARATOR + "1" +
               DATA_ELEMENT_SEPARATOR + interchangeRef + SEGMENT_TERMINATOR;
    }
    
    private String generateMessageReference() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyHHmmss"));
    }
    
    private String generateInterchangeReference() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyHHmmss")) + 
               (100 + random.nextInt(900));
    }
}
