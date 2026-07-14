package com.camino.auth.service;

import com.camino.auth.domain.Pilgrim;
import com.camino.auth.domain.PilgrimRepository;
import com.camino.auth.dto.SignupRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PilgrimRepository pilgrimRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(PilgrimRepository pilgrimRepository, PasswordEncoder passwordEncoder) {
        this.pilgrimRepository = pilgrimRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long signup(SignupRequest request) {
        if (pilgrimRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 가입된 이메일입니다");
        }

        Pilgrim pilgrim = new Pilgrim(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        return pilgrimRepository.save(pilgrim).getId();
    }
}