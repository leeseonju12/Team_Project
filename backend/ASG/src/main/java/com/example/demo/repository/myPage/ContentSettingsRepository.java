// 수정 부분만 — findByUser_Id 메서드 추가
package com.example.demo.repository.myPage;

import com.example.demo.domain.ContentSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContentSettingsRepository extends JpaRepository<ContentSettings, Long> {
    Optional<ContentSettings> findByUser_Id(Long userId);
}