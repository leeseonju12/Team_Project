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
    @Column(name = "feedback_id")
    private Long id;

    /*
    // 어떤 브랜드의 어떤 플랫폼에 달린 피드백인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_platform_id")
    private BrandPlatform brandPlatform; 
    */
    
    // 원본 데이터(Source)와 1:1 연결
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private FeedbackSource source;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FeedbackType type; // REVIEW, COMMENT

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


	@PrePersist
	public void prePersist() {
	    // Source의 플랫폼 정보를 보고 Type을 알아서 채워 넣음!
	    if (this.source != null && this.source.getPlatform() != null) {
	        this.type = this.source.getPlatform().getAutoType();
	    }
	    
	    // 기본값 세팅
	    if (this.status == null) this.status = FeedbackStatus.UNRESOLVED;
	    if (this.aiStatus == null) this.aiStatus = AiStatus.IDLE;
	}
}