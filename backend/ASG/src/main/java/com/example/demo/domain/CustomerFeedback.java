package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.example.demo.domain.enums.AiStatus;
import com.example.demo.domain.enums.FeedbackStatus;
import com.example.demo.domain.enums.FeedbackType;
import com.example.demo.domain.enums.PlatformCode;

@Entity
@Table(name = "customer_feedback")
@Data
public class CustomerFeedback {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    // 어떤 브랜드의 어떤 플랫폼에 달린 피드백인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_platform_id")
    private BrandPlatform brandPlatform; 
    */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PlatformCode platform; // NAVER_REVIEW, INSTAGRAM_COMMENT 등

    @Column(length = 100)
    private String authorName; // 작성자 (예: 김민지)

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FeedbackType type; // REVIEW, COMMENT

    @Column(columnDefinition = "TEXT")
    private String originalText; // 원문

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FeedbackStatus status; // UNRESOLVED, CHECKED, SENDING, COMPLETED

    // --- AI 답변 관련 데이터 ---
    @Column(columnDefinition = "TEXT")
    private String aiReply; // 생성된 AI 답변

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AiStatus aiStatus; // IDLE, DONE

    @Column(columnDefinition = "TEXT")
    private String sentReply; // 최종 전송된 내 답글

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}