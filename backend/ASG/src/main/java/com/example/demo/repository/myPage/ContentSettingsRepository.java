package com.example.demo.repository.myPage;

import com.example.demo.domain.ContentSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentSettingsRepository extends JpaRepository<ContentSettings, Long> {
    // user 연동 전까지 기본만 유지
}