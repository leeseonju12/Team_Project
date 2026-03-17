package com.example.demo.service.auth;

import com.example.demo.domain.auth.AuthProvider;
import com.example.demo.domain.auth.User;
import com.example.demo.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;

    /**
     * OAuth2 로그인 사용자 정보를 기준으로 기존 회원 조회 또는 신규 생성한다.
     */
    @Transactional
    public User saveOrUpdateOAuthUser(AuthProvider provider, String providerId, String email, String name) {
        validateRequiredFields(providerId, email, name);

        return userRepository.findByProviderAndProviderId(provider, providerId)
                .map(existing -> {
                    existing.updateProfile(name);
                    return existing;
                })
                .orElseGet(() -> linkByEmailOrCreate(provider, providerId, email, name));
    }

    private User linkByEmailOrCreate(AuthProvider provider, String providerId, String email, String name) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일이 없어 계정을 생성/연결할 수 없습니다.");
        }

        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.linkSocial(provider, providerId);
                    existingUser.updateProfile(name);
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(User.createSocial(email, name, provider, providerId)));
    }

    private void validateRequiredFields(String providerId, String email, String name) {
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("providerId는 필수입니다.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email은 필수입니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 필수입니다.");
        }
    }
}