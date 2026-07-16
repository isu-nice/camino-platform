package com.camino.albergue.controller;

import com.camino.albergue.domain.ReservationRepository;
import com.camino.albergue.dto.ReservationRequest;
import com.camino.albergue.dto.ReservationResponse;
import com.camino.albergue.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    public ReservationController(ReservationService reservationService, ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
    }

    @PostMapping
    public ResponseEntity<Long> reserve(@Valid @RequestBody ReservationRequest request) {
        Long id = reservationService.reserve(request);
        return ResponseEntity.ok(id);
    }

    @GetMapping
    public List<ReservationResponse> getAll() {
        return reservationRepository.findAllWithAlbergue().stream()
                .map(r -> new ReservationResponse(r.getId(), r.getAlbergue().getName(), r.getPilgrimId()))
                .toList();
    }
}