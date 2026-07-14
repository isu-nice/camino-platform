package com.camino.albergue.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "albergues")
public class Albergue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String stageName; // 예: "Sarria", "Portomarin" — 순례길 구간(마을) 이름

    @Column(nullable = false)
    private Integer totalBeds;

    @Column(nullable = false)
    private Integer availableBeds;

    @Version
    private Long version; // 낙관적 락용

    protected Albergue() {}

    public Albergue(String name, String stageName, Integer totalBeds) {
        this.name = name;
        this.stageName = stageName;
        this.totalBeds = totalBeds;
        this.availableBeds = totalBeds;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getStageName() { return stageName; }
    public Integer getTotalBeds() { return totalBeds; }
    public Integer getAvailableBeds() { return availableBeds; }

    public void decreaseBed() {
        if (this.availableBeds <= 0) {
            throw new IllegalStateException("남은 침대가 없습니다");
        }
        this.availableBeds -= 1;
    }
}