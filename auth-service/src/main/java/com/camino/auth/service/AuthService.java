package com.camino.auth.service;

import com.camino.auth.domain.Pilgrim;
import com.camino.auth.domain.PilgrimRepository;
import com.camino.auth.dto.LoginRequest;
import com.camino.auth.dto.SignupRequest;
import com.camino.auth.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PilgrimRepository pilgrimRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(PilgrimRepository pilgrimRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider1) {
        this.pilgrimRepository = pilgrimRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider1;
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


    public String login(LoginRequest request) {
        Pilgrim pilgrim = pilgrimRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), pilgrim.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        return jwtTokenProvider.createToken(pilgrim.getEmail());
    }
}