package com.pnrgov.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "travel_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    private String documentType;

    private String documentNumber;

    private String issuingCountry;

    private LocalDate expiryDate;

    private LocalDate issueDate;

    private String nationality;

    public Long getPassengerId() { return passenger != null ? passenger.getId() : null; }
}
