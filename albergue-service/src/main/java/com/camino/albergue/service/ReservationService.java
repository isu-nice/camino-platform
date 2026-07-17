package com.camino.albergue.service;

import com.camino.albergue.domain.*;
import com.camino.albergue.dto.ReservationRequest;
import com.camino.albergue.event.ReservationEvent;
import com.camino.albergue.event.ReservationEventPublisher;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class ReservationService {

    private final AlbergueRepository albergueRepository;
    private final ReservationRepository reservationRepository;
    private final RedissonClient redissonClient;
    private final ReservationEventPublisher eventPublisher;

    public ReservationService(AlbergueRepository albergueRepository,
                              ReservationRepository reservationRepository,
                              RedissonClient redissonClient,
                              ReservationEventPublisher eventPublisher) {
        this.albergueRepository = albergueRepository;
        this.reservationRepository = reservationRepository;
        this.redissonClient = redissonClient;
        this.eventPublisher = eventPublisher;
    }

    public Long reserve(ReservationRequest request) {
        String lockKey = "lock:albergue:" + request.getAlbergueId();
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            // 최대 2초 대기해서 락 획득 시도, 획득하면 최대 5초간 보유
            acquired = lock.tryLock(2, 5, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("요청이 몰려 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }
            return doReserve(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("요청 처리 중 인터럽트가 발생했습니다.");
        } finally {
            if (acquired) {
                lock.unlock();
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
        Reservation saved = reservationRepository.save(reservation);

        eventPublisher.publish(new ReservationEvent(
                saved.getId(),
                saved.getPilgrimId(),
                albergue.getName(),
                saved.getReservationDate().toString()
        ));

        return saved.getId();
    }
}