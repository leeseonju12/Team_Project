package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class SocialMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🌟 핵심 추가: 이 통계의 주인이 누구인지 식별 (User 엔티티와 매핑하거나 ID만 저장)
    @Column(nullable = false)
    private Long userId;

    private String platform; // 페북 or 인스타
    private String targetType;    // "PAGE" (계정 단위) 또는 "POST" (게시물 단위)
    private String targetId;      // 페이지 ID 또는 게시물 ID

    private LocalDate postPublishedDate;
    private LocalDateTime lastSyncedAt;

    // 하단 4개 지표 플랫폼 정책 상 집계할 수 없는 데이터에 대하여 null임...
    private Integer likesCount;      // 좋아요 수
    private Integer commentsCount;    // 댓글 수
    private Integer sharesCount;      // 공유 수
    private Integer viewsCount;       // 조회 수
}