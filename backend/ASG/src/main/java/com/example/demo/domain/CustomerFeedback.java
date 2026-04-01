package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.example.demo.domain.enums.AiStatus;
import com.example.demo.domain.enums.FeedbackStatus;
import com.example.demo.domain.enums.FeedbackType;
import com.example.demo.domain.enums.Platform;
import com.fasterxml.jackson.annotation.JsonProperty;

@Builder
@Entity
@Table(name = "customer_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    
    @Column(unique = true, name = "external_id")
    private String externalId;    // SNS 고유 댓글 ID

    private String authorName;
    
    @Column(columnDefinition = "TEXT")
    private String originalText;

    private String originUrl;

    @Enumerated(EnumType.STRING)
    private Platform platform;


	@PrePersist
	public void prePersist() {
	    // Source의 플랫폼 정보를 보고 Type을 알아서 채워 넣음!
		if (this.platform != null) {
	        this.type = this.platform.getAutoType();
	    }
	    
	    // 기본값 세팅
	    if (this.status == null) this.status = FeedbackStatus.UNRESOLVED;
	    if (this.aiStatus == null) this.aiStatus = AiStatus.IDLE;
	    if (this.createdAt == null) this.createdAt = LocalDateTime.now();
	}
	
	// 💡 AI 답변 업데이트용 비즈니스 메서드
    public void updateAiReply(String aiReply) {
        this.aiReply = aiReply;
        this.aiStatus = AiStatus.DONE;
    }
    
 // 💡 전송 완료 처리용 메서드
    public void sendReply() {
        if (this.aiReply == null || this.aiReply.trim().isEmpty()) {
            throw new IllegalStateException("전송할 답변이 없습니다.");
        }
        this.sentReply = this.aiReply; // AI 답변을 최종 전송 답변으로 확정
        this.status = FeedbackStatus.COMPLETED; // 상태를 '전송 완료'로 변경
    }
    
}