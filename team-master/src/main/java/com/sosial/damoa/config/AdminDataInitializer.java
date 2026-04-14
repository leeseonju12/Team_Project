package com.sosial.damoa.config;

import com.sosial.damoa.entity.AdminUser;
import com.sosial.damoa.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminUserRepository.findByLoginId("admin").isEmpty()) {
            AdminUser admin = new AdminUser();
            admin.setLoginId("admin");
            admin.setPassword(passwordEncoder.encode("1234"));
            admin.setName("관리자");
            adminUserRepository.save(admin);
        }
    }
}