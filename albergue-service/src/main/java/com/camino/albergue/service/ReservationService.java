package com.camino.albergue.service;

import com.camino.albergue.domain.*;
import com.camino.albergue.dto.ReservationRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final AlbergueRepository albergueRepository;
    private final ReservationRepository reservationRepository;

    private static final int MAX_RETRY = 5;

    public ReservationService(AlbergueRepository albergueRepository, ReservationRepository reservationRepository) {
        this.albergueRepository = albergueRepository;
        this.reservationRepository = reservationRepository;
    }

    public Long reserve(ReservationRequest request) {
        int attempts = 0;
        while (true) {
            try {
                return doReserve(request);
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRY) {
                    throw e; // 최대 재시도 넘으면 그때는 진짜 409로 응답
                }
                // 짧게 대기 후 재시도 (동시에 재시도가 또 몰리는 걸 완화)
                try {
                    Thread.sleep(10L * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Transactional
    protected Long doReserve(ReservationRequest request) {
        Albergue albergue = albergueRepository.findById(request.getAlbergueId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알베르게입니다"));

        albergue.decreaseBed();
        albergueRepository.save(albergue);

        Reservation reservation = new Reservation(albergue, request.getPilgrimId(), request.getReservationDate());
        return reservationRepository.save(reservation).getId();
    }
}