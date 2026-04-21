package com.example.demo.repository.myPage;

import com.example.demo.entity.myPage.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
    List<Platform> findByPlatformCodeIn(List<String> codes);
}