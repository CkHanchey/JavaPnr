package com.pnrgov.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private String paymentType;

    private String cardType;

    private String cardNumber;

    private LocalDate expiryDate;

    private String cardHolderName;

    private BigDecimal amount;

    private String currency;

    private LocalDateTime paymentDate;

    public Long getReservationId() { return reservation != null ? reservation.getId() : null; }
}
