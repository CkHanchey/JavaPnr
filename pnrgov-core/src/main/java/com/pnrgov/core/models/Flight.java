package com.pnrgov.core.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String flightNumber;

    private String airlineCode;

    private String departureAirport;

    private String arrivalAirport;

    private LocalDateTime departureDate;

    private LocalDateTime arrivalDate;

    private String aircraftType;

    private String serviceClass;

    private String operatingCarrier;

    /**
     * Operating carrier's own flight number (may differ from marketing flight number for codeshares).
     * If null, falls back to flightNumber.
     */
    private String operatingFlightNumber;

    private String flightStatus;

    private Integer segmentNumber;
}
