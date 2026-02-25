package com.pnrgov.api.controllers;

import com.pnrgov.api.dto.EdifactResponse;
import com.pnrgov.api.dto.ManifestRequest;
import com.pnrgov.api.dto.ManifestResponse;
import com.pnrgov.core.models.Reservation;
import com.pnrgov.core.repositories.ReservationRepository;
import com.pnrgov.core.services.EdifactGenerator;
import com.pnrgov.core.services.FlightManifestGenerator;
import com.pnrgov.core.services.SampleDataGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Edifact")
@RestController
@RequestMapping("/api/Edifact")
public class EdifactController {

    private final SampleDataGenerator sampleDataGenerator;
    private final EdifactGenerator edifactGenerator;
    private final FlightManifestGenerator flightManifestGenerator;
    private final ReservationRepository reservationRepository;

    public EdifactController(SampleDataGenerator sampleDataGenerator,
                             EdifactGenerator edifactGenerator,
                             FlightManifestGenerator flightManifestGenerator,
                             ReservationRepository reservationRepository) {
        this.sampleDataGenerator = sampleDataGenerator;
        this.edifactGenerator = edifactGenerator;
        this.flightManifestGenerator = flightManifestGenerator;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/generate/{reservationId}")
    public ResponseEntity<EdifactResponse> generateEdifactById(@PathVariable Long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    String edifactContent = edifactGenerator.generatePnrGov(reservation, "USCBP");
                    return ResponseEntity.ok(EdifactResponse.builder()
                            .reservationId(reservation.getId())
                            .recordLocator(reservation.getRecordLocator())
                            .edifactContent(edifactContent)
                            .generatedAt(LocalDateTime.now())
                            .build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/download/{reservationId}")
    public ResponseEntity<byte[]> downloadEdifact(@PathVariable Long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    String edifactContent = edifactGenerator.generatePnrGov(reservation, "USCBP");
                    byte[] bytes = edifactContent.getBytes();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + reservation.getRecordLocator() + ".edi\"")
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(bytes);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/manifest/generate")
    public ResponseEntity<ManifestResponse> generateManifest(@RequestBody(required = false) ManifestRequest request) {
        if (request == null) request = new ManifestRequest();
        int pnrCount = request.getPassengerCount() > 0 ? request.getPassengerCount() : 5;
        String receiver = request.getReceiver() != null ? request.getReceiver() : "USCBP";
        try {
            String edifactContent = flightManifestGenerator.generateFlightManifest(
                    pnrCount, request.getAirline(), request.getFlightNumber(), receiver);
            return ResponseEntity.ok(ManifestResponse.builder()
                    .edifactContent(edifactContent)
                    .passengerCount(pnrCount)
                    .generatedAt(LocalDateTime.now())
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/manifest/download")
    public ResponseEntity<byte[]> downloadManifest(@RequestBody(required = false) ManifestRequest request) {
        if (request == null) request = new ManifestRequest();
        int pnrCount = request.getPassengerCount() > 0 ? request.getPassengerCount() : 5;
        String receiver = request.getReceiver() != null ? request.getReceiver() : "USCBP";
        try {
            String edifactContent = flightManifestGenerator.generateFlightManifest(
                    pnrCount, request.getAirline(), request.getFlightNumber(), receiver);
            String airlineStr = request.getAirline() != null ? request.getAirline() : "XX";
            String flightStr  = request.getFlightNumber() != null ? request.getFlightNumber() : "0000";
            String fileName = "PNRGOV_Manifest_" + airlineStr + flightStr
                    + "_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".edi";
            byte[] bytes = edifactContent.getBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(bytes);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<EdifactResponse> generateEdifact() {
        Reservation reservation = sampleDataGenerator.generateRandomReservation();
        String edifactContent = edifactGenerator.generatePnrGov(reservation, "USCBP");
        return ResponseEntity.ok(EdifactResponse.builder()
                .recordLocator(reservation.getRecordLocator())
                .edifactContent(edifactContent)
                .generatedAt(LocalDateTime.now())
                .build());
    }
}
