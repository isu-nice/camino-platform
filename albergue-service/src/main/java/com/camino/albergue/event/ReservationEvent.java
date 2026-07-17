package com.camino.albergue.event;

public class ReservationEvent {
    private final Long reservationId;
    private final Long pilgrimId;
    private final String albergueName;
    private final String reservationDate;

    public ReservationEvent(Long reservationId, Long pilgrimId, String albergueName, String reservationDate) {
        this.reservationId = reservationId;
        this.pilgrimId = pilgrimId;
        this.albergueName = albergueName;
        this.reservationDate = reservationDate;
    }

    public Long getReservationId() { return reservationId; }
    public Long getPilgrimId() { return pilgrimId; }
    public String getAlbergueName() { return albergueName; }
    public String getReservationDate() { return reservationDate; }
}