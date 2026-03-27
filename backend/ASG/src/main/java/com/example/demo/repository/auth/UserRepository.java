package com.example.demo.repository.auth;

import com.example.demo.domain.user.entity.User;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // 핵심 조회: provider + providerId 기반 식별
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 동일 이메일 다른 provider 체크용
    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);
}