package com.pnrgov.core.services;

import com.pnrgov.core.models.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Generates random sample data for testing PNRGOV generation
 */
@Service
public class SampleDataGenerator {
    private static final Random random = new Random();

    private static final String[] FIRST_NAMES = {
            "JOHN", "JANE", "MICHAEL", "SARAH", "DAVID", "EMILY", "ROBERT", "LISA",
            "WILLIAM", "JENNIFER", "JAMES", "MARIA", "THOMAS", "ANNA", "DANIEL", "EMMA",
            "KRISTJAN", "GUDRUN", "SIGURDUR", "HELGA", "OLAFUR", "BJORK", "MAGNUS", "HANNA",
            "SVEN", "INGRID", "LARS", "ASTRID", "ANDERS", "SOFIA", "HENRIK", "ELSA"
    };

    private static final String[] LAST_NAMES = {
            "SMITH", "JOHNSON", "WILLIAMS", "BROWN", "JONES", "GARCIA", "MILLER", "DAVIS",
            "RODRIGUEZ", "MARTINEZ", "HERNANDEZ", "LOPEZ", "GONZALEZ", "WILSON", "ANDERSON", "TAYLOR",
            "JONSSON", "KARLSSON", "NIELSEN", "HANSEN", "OLSEN", "PETERSEN", "LARSEN", "ERIKSSON",
            "MAGNUSSON", "STEFANSSON", "GUNNARSSON", "JOHANNSSON", "SIGURDSSON", "BJORNSSON"
    };

    private static final String[] TITLES = {"MR", "MRS", "MS", "MISS", "DR"};

    private static final String[] AIRLINES = {
            "FI", "W6", "SK", "OG", "W4", "BA", "LH", "AF", "KL", "DL",
            "AA", "UA", "EK", "QF", "SQ", "AY", "IB", "LX", "OS", "SN"
    };

    private static final String[] AIRPORTS = {
            "KEF", "CPH", "ARN", "OSL", "HEL", "RIX", "TLL", "VNO", "WAW", "PRG", "BUD",
            "LHR", "LGW", "STN", "MAN", "EDI", "GLA", "DUB", "CRK", "SNN",
            "CDG", "ORY", "LYS", "NCE", "MPL",
            "AMS", "RTM", "EIN", "BRU", "BLL", "ZRH", "VIE", "LIS", "OPO",
            "FRA", "DHM", "MUC", "BER", "COL", "DUS", "HAM", "GBF", "BRE",
            "MAD", "SVQ", "AGP", "VLC", "IBZ", "PMI", "ALC", "BCN",
            "MXP", "MIL", "VCE", "BOL", "FCO", "CIA", "NAP", "PMO", "TRN",
            "ATH", "IST", "BEG",
            "JFK", "LGA", "EWR", "BOS", "PHL", "WAS", "IAD", "BNA", "ATL", "TPA", "MIA", "FLL", "MCO",
            "ORD", "MDW", "DTW", "CLE", "IND", "MSY", "MEM", "AUS", "SAT", "HOU", "IAH", "DFW", "DAL",
            "DEN", "PHX", "LAS", "SLC", "SFO", "SJC", "OAK", "LAX", "LGB", "ONT", "PDX",
            "YYZ", "YUL", "YVR", "YEG", "YWG", "YYJ",
            "MEX", "CUN", "PVR", "CZM", "XEL",
            "GIG", "SDU", "GRU", "VCP", "EZE", "AEP", "SCL", "MVD", "LIM", "BOG", "MDE", "CTG", "CCS"
    };

    private static final String[] COUNTRIES = {
            "IS", "US", "GB", "DE", "FR", "ES", "IT", "CA", "SE", "NO",
            "DK", "FI", "NL", "BE", "CH", "AT", "PL", "LT", "LV", "EE",
            "BR", "AR", "CL", "PE", "CO", "VE", "UY", "PY", "BO", "EC", "MX"
    };

    private static final String[] CITIES = {
            "REYKJAVIK", "COPENHAGEN", "LONDON", "PARIS", "FRANKFURT", "AMSTERDAM",
            "BRUSSELS", "ZURICH", "STOCKHOLM", "OSLO", "VILNIUS", "ROME", "MADRID",
            "NEW YORK", "LOS ANGELES", "CHICAGO", "BOSTON", "TORONTO", "MONTREAL"
    };

    private static final String[] CARD_TYPES = {"VI", "CA", "AX", "DC", "MC"};

    private static final String[] EMAIL_DOMAINS = {
            "gmail.com", "hotmail.com", "yahoo.com", "outlook.com", "icloud.com",
            "example.com", "mail.com", "protonmail.com"
    };

    private static final String[] PHONE_COUNTRY_CODES = {
            "354", "45", "46", "47", "44", "33", "49", "31", "32",
            "41", "1", "370", "372", "371", "358"
    };

    public Reservation generateRandomReservation() {
        return generateRandomReservation(2, 2, true, true, true, true, false, false, true, true, true);
    }

    public Reservation generateRandomReservation(
            int passengerCount,
            int flightCount,
            boolean includeBags,
            boolean includeSeats,
            boolean includeDocuments,
            boolean includePayment,
            boolean includeCodeshare,
            boolean includeThruFlight,
            boolean includePhoneNumbers,
            boolean includeAgencyInfo,
            boolean includeCreditCard) {

        String contactFirstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String contactLastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String emailDomain = EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
        String phoneCountryCode = PHONE_COUNTRY_CODES[random.nextInt(PHONE_COUNTRY_CODES.length)];

        Reservation reservation = Reservation.builder()
                .recordLocator(generateRecordLocator())
                .bookingDate(LocalDate.now().minusDays(random.nextInt(30) + 1))
                .createdDate(LocalDateTime.now().minusDays(random.nextInt(30) + 1))
                .bookingChannel("WEB")
                .agencyCode(includeAgencyInfo ? generateAgencyCode() : "")
                .status("HK")
                .contactFirstName(contactFirstName)
                .contactLastName(contactLastName)
                .contactEmail(contactFirstName.toLowerCase() + "." + contactLastName.toLowerCase() + "@" + emailDomain)
                .contactPhone(includePhoneNumbers ? phoneCountryCode + random.nextInt(9000000) + 1000000 : "")
                .build();

        int segmentNumber = 1;

        if (includeThruFlight && flightCount > 0) {
            String thruFlightNumber = String.valueOf(random.nextInt(9900) + 100);
            String thruAirline = AIRLINES[random.nextInt(AIRLINES.length)];
            LocalDateTime departureDate = LocalDateTime.now().plusDays(random.nextInt(60) + 1);

            String firstAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];
            String middleAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];
            while (middleAirport.equals(firstAirport)) {
                middleAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];
            }

            reservation.getFlights().add(Flight.builder()
                    .flightNumber(thruFlightNumber)
                    .airlineCode(thruAirline)
                    .departureAirport(firstAirport)
                    .arrivalAirport(middleAirport)
                    .departureDate(departureDate)
                    .arrivalDate(departureDate.plusHours(random.nextInt(4) + 2))
                    .aircraftType(random.nextInt(2) == 0 ? "738" : "777")
                    .serviceClass("Y")
                    .operatingCarrier(thruAirline)
                    .flightStatus("HK")
                    .segmentNumber(segmentNumber++)
                    .build());

            String finalAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];
            while (finalAirport.equals(middleAirport) || finalAirport.equals(firstAirport)) {
                finalAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];
            }

            LocalDateTime secondLegDeparture = departureDate.plusHours(random.nextInt(4) + 3);
            reservation.getFlights().add(Flight.builder()
                    .flightNumber(thruFlightNumber)
                    .airlineCode(thruAirline)
                    .departureAirport(middleAirport)
                    .arrivalAirport(finalAirport)
                    .departureDate(secondLegDeparture)
                    .arrivalDate(secondLegDeparture.plusHours(random.nextInt(4) + 2))
                    .aircraftType(random.nextInt(2) == 0 ? "738" : "777")
                    .serviceClass("Y")
                    .operatingCarrier(thruAirline)
                    .flightStatus("HK")
                    .segmentNumber(segmentNumber++)
                    .build());

            flightCount = Math.max(0, flightCount - 2);
        }

        if (includeCodeshare && flightCount > 0) {
            String marketingCarrier = AIRLINES[random.nextInt(AIRLINES.length)];
            String operatingCarrier = AIRLINES[random.nextInt(AIRLINES.length)];
            while (operatingCarrier.equals(marketingCarrier)) {
                operatingCarrier = AIRLINES[random.nextInt(AIRLINES.length)];
            }

            String departureAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];
            String arrivalAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];
            while (arrivalAirport.equals(departureAirport)) {
                arrivalAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];
            }

            LocalDateTime departureDate = LocalDateTime.now().plusDays(random.nextInt(60) + 1);
            String serviceClass = switch (random.nextInt(3)) {
                case 1 -> "C";
                case 2 -> "F";
                default -> "Y";
            };

            reservation.getFlights().add(Flight.builder()
                    .flightNumber(String.valueOf(random.nextInt(9900) + 100))
                    .airlineCode(marketingCarrier)
                    // Operating carrier uses its own distinct flight number
                    .operatingFlightNumber(String.valueOf(random.nextInt(9900) + 100))
                    .departureAirport(departureAirport)
                    .arrivalAirport(arrivalAirport)
                    .departureDate(departureDate)
                    .arrivalDate(departureDate.plusHours(random.nextInt(13) + 2))
                    .aircraftType(random.nextInt(2) == 0 ? "738" : "777")
                    .serviceClass(serviceClass)
                    .operatingCarrier(operatingCarrier)
                    .flightStatus("HK")
                    .segmentNumber(segmentNumber++)
                    .build());

            flightCount--;
        }

        String currentArrivalAirport = reservation.getFlights().isEmpty() ?
                AIRPORTS[random.nextInt(AIRPORTS.length)] :
                reservation.getFlights().get(reservation.getFlights().size() - 1).getArrivalAirport();

        for (int i = 0; i < flightCount; i++) {
            Flight flight = generateConnectedFlight(segmentNumber++, currentArrivalAirport);
            reservation.getFlights().add(flight);
            currentArrivalAirport = flight.getArrivalAirport();
        }

        for (int i = 0; i < passengerCount; i++) {
            Passenger passenger = generateRandomPassenger(includePhoneNumbers);

            if (includeDocuments) {
                passenger.getDocuments().add(generateRandomDocument(passenger));
            }

            if (includeBags) {
                for (Flight flight : reservation.getFlights()) {
                    passenger.getBags().add(generateRandomBaggage(passenger, flight));
                }
            }

            if (includeSeats) {
                for (Flight flight : reservation.getFlights()) {
                    passenger.getSeats().add(generateRandomSeat(passenger, flight));
                }
            }

            reservation.getPassengers().add(passenger);
        }

        if (includePayment && includeCreditCard) {
            reservation.getPayments().add(generateRandomPayment(reservation));
        }

        return reservation;
    }

    private Flight generateConnectedFlight(int segmentNumber, String departureAirport) {
        String airline = AIRLINES[random.nextInt(AIRLINES.length)];
        String arrivalAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];

        while (arrivalAirport.equals(departureAirport)) {
            arrivalAirport = AIRPORTS[random.nextInt(AIRPORTS.length)];
        }

        LocalDateTime departureDate = LocalDateTime.now().plusDays(random.nextInt(60) + 1);
        String serviceClass = switch (random.nextInt(3)) {
            case 1 -> "C";
            case 2 -> "F";
            default -> "Y";
        };

        return Flight.builder()
                .flightNumber(String.valueOf(random.nextInt(9900) + 100))
                .airlineCode(airline)
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .departureDate(departureDate)
                .arrivalDate(departureDate.plusHours(random.nextInt(13) + 2))
                .aircraftType(random.nextInt(2) == 0 ? "738" : "777")
                .serviceClass(serviceClass)
                .operatingCarrier(airline)
                .flightStatus("HK")
                .segmentNumber(segmentNumber)
                .build();
    }

    private Passenger generateRandomPassenger(boolean includePhone) {
        String gender = random.nextInt(2) == 0 ? "M" : "F";
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
        String city = CITIES[random.nextInt(CITIES.length)];
        String emailDomain = EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
        String phoneCountryCode = PHONE_COUNTRY_CODES[random.nextInt(PHONE_COUNTRY_CODES.length)];

        String title = gender.equals("M") ? "MR" : TITLES[random.nextInt(1, TITLES.length)];

        return Passenger.builder()
                .firstName(firstName)
                .lastName(lastName)
                .middleName(random.nextInt(2) == 0 ? "" : String.valueOf(FIRST_NAMES[random.nextInt(FIRST_NAMES.length)].charAt(0)))
                .title(title)
                .dateOfBirth(LocalDate.now().minusYears(random.nextInt(52) + 18))
                .gender(gender)
                .nationality(country)
                .passengerType("ADT")
                .email(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + emailDomain)
                .phone(includePhone ? phoneCountryCode + random.nextInt(9000000) + 1000000 : "")
                .addressLine1(random.nextInt(999) + 1 + " " + getStreetName())
                .addressLine2(random.nextInt(3) == 0 ? "" : "APT " + (random.nextInt(199) + 1))
                .city(city)
                .state(getStateOrRegion(country))
                .postalCode(getPostalCode(country))
                .country(country)
                .build();
    }

    private TravelDocument generateRandomDocument(Passenger passenger) {
        String issuingCountry = COUNTRIES[random.nextInt(COUNTRIES.length)];

        return TravelDocument.builder()
                .passenger(passenger)
                .documentType("P")
                .documentNumber(issuingCountry + random.nextInt(900000000) + 100000000)
                .issuingCountry(issuingCountry)
                .expiryDate(LocalDate.now().plusYears(random.nextInt(9) + 1))
                .issueDate(LocalDate.now().minusYears(random.nextInt(5) + 1))
                .nationality(issuingCountry)
                .build();
    }

    private Baggage generateRandomBaggage(Passenger passenger, Flight flight) {
        return Baggage.builder()
                .passenger(passenger)
                .flight(flight)
                .bagTagNumber(String.valueOf(random.nextInt(900000) + 100000))
                .weight(BigDecimal.valueOf(random.nextInt(17) + 15))
                .weightUnit("KG")
                .numberOfPieces(random.nextInt(2) + 1)
                .baggageType("Checked")
                .status("Checked-in")
                .build();
    }

    private SeatAssignment generateRandomSeat(Passenger passenger, Flight flight) {
        int row = random.nextInt(39) + 1;
        char seatLetter = (char) ('A' + random.nextInt(6));

        String characteristics = switch (seatLetter) {
            case 'A', 'F' -> "Window";
            case 'C', 'D' -> "Aisle";
            default -> "Middle";
        };

        return SeatAssignment.builder()
                .passenger(passenger)
                .flight(flight)
                .seatNumber(row + String.valueOf(seatLetter))
                .seatCharacteristics(characteristics)
                .build();
    }

    private Payment generateRandomPayment(Reservation reservation) {
        String cardType = CARD_TYPES[random.nextInt(CARD_TYPES.length)];

        return Payment.builder()
                .reservation(reservation)
                .paymentType("CC")
                .cardType(cardType)
                .cardNumber("****" + random.nextInt(9000) + 1000)
                .expiryDate(LocalDate.now().plusYears(random.nextInt(4) + 1))
                .cardHolderName(FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " + LAST_NAMES[random.nextInt(LAST_NAMES.length)])
                .amount(BigDecimal.valueOf(random.nextInt(4500) + 500))
                .currency("USD")
                .paymentDate(LocalDateTime.now().minusDays(random.nextInt(30) + 1))
                .build();
    }

    private String generateRecordLocator() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }

    private String generateAgencyCode() {
        return String.valueOf(random.nextInt(90000000) + 10000000);
    }

    private String getStreetName() {
        String[] streetNames = {
                "MAIN STREET", "HIGH STREET", "CHURCH ROAD", "STATION ROAD", "PARK AVENUE",
                "MARKET STREET", "SAEBRAUT", "LAUGAVEGUR", "SKOLAVORDUSTIGUR"
        };
        return streetNames[random.nextInt(streetNames.length)];
    }

    private String getStateOrRegion(String country) {
        return switch (country) {
            case "US" -> new String[]{"CA", "NY", "FL", "TX", "IL", "WA"}[random.nextInt(6)];
            default -> "";
        };
    }

    private String getPostalCode(String country) {
        return switch (country) {
            case "US" -> String.valueOf(random.nextInt(90000) + 10000);
            case "GB" -> String.valueOf((char) ('A' + random.nextInt(26))) + (char) ('A' + random.nextInt(26)) +
                    random.nextInt(9) + 1 + " " + random.nextInt(9) + (char) ('A' + random.nextInt(26)) +
                    (char) ('A' + random.nextInt(26));
            case "IS", "DK", "NO", "SE" -> String.valueOf(random.nextInt(9000) + 1000);
            case "DE", "FR", "IT", "ES" -> String.valueOf(random.nextInt(90000) + 10000);
            case "NL" -> String.valueOf(random.nextInt(9000) + 1000) + " " + (char) ('A' + random.nextInt(26)) +
                    (char) ('A' + random.nextInt(26));
            default -> String.valueOf(random.nextInt(90000) + 10000);
        };
    }
}
