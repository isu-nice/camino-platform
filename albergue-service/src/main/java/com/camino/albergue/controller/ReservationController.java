package com.camino.albergue.controller;

import com.camino.albergue.dto.ReservationRequest;
import com.camino.albergue.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Long> reserve(@Valid @RequestBody ReservationRequest request) {
        Long id = reservationService.reserve(request);
        return ResponseEntity.ok(id);
    }
}