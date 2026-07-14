package com.camino.albergue.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ReservationRequest {

    @NotNull
    private Long albergueId;

    @NotNull
    private Long pilgrimId;

    @NotNull
    private LocalDate reservationDate;

    public Long getAlbergueId() { return albergueId; }
    public Long getPilgrimId() { return pilgrimId; }
    public LocalDate getReservationDate() { return reservationDate; }
}