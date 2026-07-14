package com.camino.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PilgrimRepository extends JpaRepository<Pilgrim, Long> {
    Optional<Pilgrim> findByEmail(String email);
    boolean existsByEmail(String email);
}