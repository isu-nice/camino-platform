package com.camino.albergue.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "albergue_id", nullable = false)
    private Albergue albergue;

    @Column(nullable = false)
    private Long pilgrimId; // Auth Service의 Pilgrim ID (서비스가 분리돼 있어서 직접 참조 대신 ID만 저장)

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Reservation() {}

    public Reservation(Albergue albergue, Long pilgrimId, LocalDate reservationDate) {
        this.albergue = albergue;
        this.pilgrimId = pilgrimId;
        this.reservationDate = reservationDate;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Albergue getAlbergue() { return albergue; }
    public Long getPilgrimId() { return pilgrimId; }
    public LocalDate getReservationDate() { return reservationDate; }
}