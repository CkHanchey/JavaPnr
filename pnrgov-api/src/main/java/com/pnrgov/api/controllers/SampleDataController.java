package com.pnrgov.api.controllers;

import com.pnrgov.api.dto.*;
import com.pnrgov.core.models.Reservation;
import com.pnrgov.core.repositories.ReservationRepository;
import com.pnrgov.core.services.SampleDataGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "SampleData")
@RestController
@RequestMapping("/api/SampleData")
public class SampleDataController {
    
    private final ReservationRepository reservationRepository;
    private final SampleDataGenerator sampleDataGenerator;
    
    public SampleDataController(ReservationRepository reservationRepository, 
                               SampleDataGenerator sampleDataGenerator) {
        this.reservationRepository = reservationRepository;
        this.sampleDataGenerator = sampleDataGenerator;
    }
    
    /**
     * Generate a random sample reservation
     */
    @PostMapping("/generate")
    public ResponseEntity<SampleDataResponse> generateSampleData(@RequestBody SampleDataRequest request) {
        Reservation reservation = sampleDataGenerator.generateRandomReservation(
            request.getPassengerCount(),
            request.getFlightCount(),
            request.isIncludeBags(),
            request.isIncludeSeats(),
            request.isIncludeDocuments(),
            request.isIncludePayment(),
            request.isIncludeCodeshare(),
            request.isIncludeThruFlight(),
            request.isIncludePhoneNumbers(),
            request.isIncludeAgencyInfo(),
            request.isIncludeCreditCard()
        );
        
        reservation = reservationRepository.save(reservation);
        
        SampleDataResponse response = SampleDataResponse.builder()
            .reservationId(reservation.getId())
            .recordLocator(reservation.getRecordLocator())
            .passengerCount(reservation.getPassengers().size())
            .flightCount(reservation.getFlights().size())
            .message("Sample reservation created successfully")
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Generate multiple random sample reservations
     */
    @PostMapping("/generate-multiple")
    public ResponseEntity<MultipleSampleDataResponse> generateMultipleSamples(
            @RequestBody MultipleSampleDataRequest request) {
        
        int count = request.getCount() > 0 ? request.getCount() : 5;
        int passengerCount = request.getPassengerCount() > 0 ? request.getPassengerCount() : 2;
        int flightCount = request.getFlightCount() > 0 ? request.getFlightCount() : 2;
        boolean includeBags = request.isIncludeBags();
        boolean includeSeats = request.isIncludeSeats();
        boolean includeDocuments = request.isIncludeDocuments();
        boolean includePayment = request.isIncludePayment();
        boolean includeCodeshare = request.isIncludeCodeshare();
        boolean includeThruFlight = request.isIncludeThruFlight();
        boolean includePhoneNumbers = request.isIncludePhoneNumbers();
        boolean includeAgencyInfo = request.isIncludeAgencyInfo();
        boolean includeCreditCard = request.isIncludeCreditCard();
        
        List<SampleDataResponse> generatedReservations = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Reservation reservation = sampleDataGenerator.generateRandomReservation(
                passengerCount,
                flightCount,
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
            
            reservation = reservationRepository.save(reservation);
            
            generatedReservations.add(SampleDataResponse.builder()
                .reservationId(reservation.getId())
                .recordLocator(reservation.getRecordLocator())
                .passengerCount(reservation.getPassengers().size())
                .flightCount(reservation.getFlights().size())
                .build());
        }
        
        MultipleSampleDataResponse response = MultipleSampleDataResponse.builder()
            .totalGenerated(generatedReservations.size())
            .reservations(generatedReservations)
            .message("Successfully generated " + generatedReservations.size() + " sample reservations")
            .build();
        
        return ResponseEntity.ok(response);
    }
}
