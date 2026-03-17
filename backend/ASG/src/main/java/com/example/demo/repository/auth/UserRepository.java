package com.example.demo.repository.auth;

import com.example.demo.domain.auth.AuthProvider;
import com.example.demo.domain.auth.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}