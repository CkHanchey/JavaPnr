package com.pnrgov.api.controllers;

import com.pnrgov.core.models.Reservation;
import com.pnrgov.core.repositories.ReservationRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Reservations")
@RestController
@RequestMapping("/api/Reservations")
public class ReservationsController {
    
    private final ReservationRepository reservationRepository;
    
    public ReservationsController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
    
    /**
     * Get all reservations
     */
    @GetMapping
    public ResponseEntity<List<Reservation>> getReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return ResponseEntity.ok(reservations);
    }
    
    /**
     * Create a new reservation
     */
    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        Reservation savedReservation = reservationRepository.save(reservation);
        return ResponseEntity.created(
            URI.create("/api/reservations/" + savedReservation.getId()))
            .body(savedReservation);
    }
    
    /**
     * Get a specific reservation by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update an existing reservation
     */
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable Long id, 
            @RequestBody Reservation reservation) {
        
        if (!id.equals(reservation.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        if (!reservationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        Reservation updatedReservation = reservationRepository.save(reservation);
        return ResponseEntity.ok(updatedReservation);
    }
    
    /**
     * Delete a reservation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        if (!reservationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        reservationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get reservation by record locator
     */
    @GetMapping("/by-locator/{recordLocator}")
    public ResponseEntity<Reservation> getReservationByLocator(@PathVariable String recordLocator) {
        return reservationRepository.findByRecordLocator(recordLocator)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete all reservations
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, Object>> deleteAllReservations() {
        long count = reservationRepository.count();
        reservationRepository.deleteAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Deleted " + count + " reservation(s)");
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
}
