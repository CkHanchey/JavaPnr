package com.pnrgov.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "baggage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Baggage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    private String bagTagNumber;

    private BigDecimal weight;

    private String weightUnit;

    private Integer numberOfPieces;

    private String baggageType;

    private String status;

    public Long getPassengerId() { return passenger != null ? passenger.getId() : null; }
    public Long getFlightId() { return flight != null ? flight.getId() : null; }
}
