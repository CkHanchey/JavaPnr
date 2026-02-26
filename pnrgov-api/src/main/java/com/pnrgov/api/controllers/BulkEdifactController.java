package com.pnrgov.api.controllers;

import com.pnrgov.api.dto.*;
import com.pnrgov.core.models.Reservation;
import com.pnrgov.core.services.EdifactGenerator;
import com.pnrgov.core.services.SampleDataGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Tag(name = "BulkEdifact")
@RestController
@RequestMapping("/api/BulkEdifact")
public class BulkEdifactController {
    
    private final SampleDataGenerator sampleGenerator;
    private final EdifactGenerator edifactGenerator;
    private final Random random = new Random();
    
    public BulkEdifactController(SampleDataGenerator sampleGenerator, EdifactGenerator edifactGenerator) {
        this.sampleGenerator = sampleGenerator;
        this.edifactGenerator = edifactGenerator;
    }
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateBulk(@RequestBody BulkGenerationRequest request) {
        // Validation
        if (request.getFileCount() < 1 || request.getFileCount() > 1000) {
            return ResponseEntity.badRequest().body("File count must be between 1 and 1000");
        }
        if (request.getMinPassengers() < 1 || request.getMaxPassengers() > 20) {
            return ResponseEntity.badRequest().body("Passenger range must be between 1 and 20");
        }
        if (request.getMinFlights() < 1 || request.getMaxFlights() > 10) {
            return ResponseEntity.badRequest().body("Flight range must be between 1 and 10");
        }
        if (request.getMinPassengers() > request.getMaxPassengers()) {
            return ResponseEntity.badRequest().body("MinPassengers cannot be greater than MaxPassengers");
        }
        if (request.getMinFlights() > request.getMaxFlights()) {
            return ResponseEntity.badRequest().body("MinFlights cannot be greater than MaxFlights");
        }
        
        List<GeneratedFile> files = new ArrayList<>();
        
        for (int i = 0; i < request.getFileCount(); i++) {
            int passengerCount = request.getMinPassengers() + 
                random.nextInt(request.getMaxPassengers() - request.getMinPassengers() + 1);
            int flightCount = request.getMinFlights() + 
                random.nextInt(request.getMaxFlights() - request.getMinFlights() + 1);
            
            // Randomize options
            boolean includeBags = random.nextInt(2) == 0;
            boolean includeSeats = random.nextInt(2) == 0;
            boolean includeDocuments = random.nextInt(2) == 0;
            boolean includePayment = random.nextInt(2) == 0;
            boolean includeCodeshare = random.nextInt(3) == 0; // Less frequent
            boolean includeThruFlight = random.nextInt(3) == 0; // Less frequent
            boolean includePhoneNumbers = random.nextInt(2) == 0;
            boolean includeAgencyInfo = random.nextInt(2) == 0;
            boolean includeCreditCard = includePayment && random.nextInt(2) == 0;
            
            Reservation reservation = sampleGenerator.generateRandomReservation(
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
            
            String edifact = edifactGenerator.generatePnrGov(reservation, request.getReceiver());
            
            GeneratedFile file = GeneratedFile.builder()
                .fileName(reservation.getRecordLocator() + ".edi")
                .recordLocator(reservation.getRecordLocator())
                .content(edifact)
                .passengerCount(passengerCount)
                .flightCount(flightCount)
                .options(FileOptions.builder()
                    .hasBags(includeBags)
                    .hasSeats(includeSeats)
                    .hasDocuments(includeDocuments)
                    .hasPayment(includePayment)
                    .isCodeshare(includeCodeshare)
                    .isThruFlight(includeThruFlight)
                    .hasPhones(includePhoneNumbers)
                    .hasAgency(includeAgencyInfo)
                    .hasCreditCard(includeCreditCard)
                    .build())
                .build();
            
            files.add(file);
        }
        
        BulkGenerationResponse response = BulkGenerationResponse.builder()
            .files(files)
            .totalFiles(files.size())
            .generatedAt(LocalDateTime.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
}
