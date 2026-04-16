package com.example.demo.repository.customerCenter;

import com.example.demo.entity.customerCenter.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByLoginId(String loginId);
}
