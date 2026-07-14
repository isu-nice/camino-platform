package com.camino.albergue.service;

import com.camino.albergue.domain.*;
import com.camino.albergue.dto.ReservationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final AlbergueRepository albergueRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(AlbergueRepository albergueRepository, ReservationRepository reservationRepository) {
        this.albergueRepository = albergueRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Long reserve(ReservationRequest request) {
        Albergue albergue = albergueRepository.findById(request.getAlbergueId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알베르게입니다"));

        albergue.decreaseBed();
        albergueRepository.save(albergue);

        Reservation reservation = new Reservation(albergue, request.getPilgrimId(), request.getReservationDate());
        return reservationRepository.save(reservation).getId();
    }
}