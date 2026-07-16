package com.camino.albergue.dto;

public class ReservationResponse {
    private final Long id;
    private final String albergueName;
    private final Long pilgrimId;

    public ReservationResponse(Long id, String albergueName, Long pilgrimId) {
        this.id = id;
        this.albergueName = albergueName;
        this.pilgrimId = pilgrimId;
    }

    public Long getId() { return id; }
    public String getAlbergueName() { return albergueName; }
    public Long getPilgrimId() { return pilgrimId; }
}