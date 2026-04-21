package com.example.demo.service.auth;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.user.entity.RefreshToken;
import com.example.demo.repository.auth.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void replaceRefreshToken(Long userId, String refreshToken, long refreshExpirationMs) {
        refreshTokenRepository.deleteByUserId(userId);
        refreshTokenRepository.save(
                new RefreshToken(
                        refreshToken,
                        userId,
                        LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000)
                )
        );
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.deleteByToken(refreshToken);
    }
}