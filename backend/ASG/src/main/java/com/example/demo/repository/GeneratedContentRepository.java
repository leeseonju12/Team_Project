package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.calendar.entity.ContentStatus;
import com.example.demo.entity.GeneratedContent;

@Repository
public interface GeneratedContentRepository extends JpaRepository<GeneratedContent, Long> {

    /**
     * 1. 미배정(Pending) 컨텐츠 조회
     * scheduledDate가 null인 데이터만 생성일 역순(최신순)으로 가져옵니다.
     * 엔티티의 필드명이 scheduledAt -> scheduledDate로 변경됨에 따라 메서드명 수정
     */
    List<GeneratedContent> findByScheduledDateIsNullOrderByCreatedAtDesc();

    /**
     * 2. 캘린더 배정된 컨텐츠 조회
     * scheduledDate가 null이 아닌(날짜가 지정된) 모든 데이터를 가져옵니다.
     */
    List<GeneratedContent> findByScheduledDateIsNotNull();

    /**
     * 3. 특정 플랫폼별 미배정 컨텐츠 필터링
     */
    List<GeneratedContent> findByPlatformAndScheduledDateIsNull(String platform);

    /**
     * 4. 상태(Status) 기반 조회
     * status 필드가 Enum(PostStatus) 타입이므로 파라미터 타입을 Enum으로 변경하는 것이 안전합니다.
     */
    List<GeneratedContent> findByStatus(ContentStatus status);
    
    /**
     * 5. 추가 제안: 특정 메뉴 이름으로 검색 (마케팅 카테고리 검색용)
     */
    List<GeneratedContent> findByMenuNameContaining(String menuName);
}