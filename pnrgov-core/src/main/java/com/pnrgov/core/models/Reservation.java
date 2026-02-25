package com.pnrgov.core.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String recordLocator;

    private LocalDate bookingDate;

    private LocalDateTime createdDate;

    private String bookingChannel;

    private String agencyCode;

    private String status;

    private String contactFirstName;

    private String contactLastName;

    private String contactEmail;

    private String contactPhone;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "reservation_id")
    @Builder.Default
    private List<Passenger> passengers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "reservation_id")
    @Builder.Default
    private List<Flight> flights = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "reservation_id")
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
}
