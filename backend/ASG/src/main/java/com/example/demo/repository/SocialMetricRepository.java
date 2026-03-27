package com.example.demo.repository;

import com.example.demo.entity.SocialMetric;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialMetricRepository extends JpaRepository<SocialMetric, Long> {

	// 🌟 특정 사용자의 특정 플랫폼(예: 인스타) 전체 통계를 날짜순으로 가져오기
    List<SocialMetric> findByUserIdAndPlatformOrderByPostPublishedDateAsc(Long userId, String platform);

    // 🌟 특정 사용자의 특정 게시물 하나에 대한 통계만 가져오기
    List<SocialMetric> findByUserIdAndTargetId(Long userId, String targetId);
    
    // db 중복검사
    Optional<SocialMetric> findByUserIdAndPlatformAndTargetId(Long userId, String platform, String targetId);
}