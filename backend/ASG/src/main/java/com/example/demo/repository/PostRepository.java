package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.calendar.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 1. 미배정(Pending) 포스트 조회
     * scheduledAt이 null인 데이터만 생성일 역순(최신순)으로 가져옵니다.
     * 패널 왼쪽 '대기 목록'에 뿌려줄 때 사용합니다.
     */
    List<Post> findByScheduledAtIsNullOrderByCreatedAtDesc();

    /**
     * 2. 캘린더 배정된 포스트 조회
     * scheduledAt이 null이 아닌(날짜가 지정된) 모든 데이터를 가져옵니다.
     * FullCalendar 초기 로드 시 이벤트를 표시할 때 사용합니다.
     */
    List<Post> findByScheduledAtIsNotNull();

    /**
     * 3. 특정 플랫폼별 미배정 포스트 필터링 (선택 사항)
     * 예: 인스타그램용 대기 포스트만 따로 보고 싶을 때 사용합니다.
     */
    List<Post> findByPlatformAndScheduledAtIsNull(String platform);

    /**
     * 4. 상태(Status) 기반 조회
     * 'PENDING', 'SCHEDULED', 'PUBLISHED' 등 상태값으로 구분하여 가져옵니다.
     */
    List<Post> findByStatus(String status);
}