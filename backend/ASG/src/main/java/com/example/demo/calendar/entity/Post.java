package com.example.demo.calendar.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false, length = 50)
    private String platform; // INSTAGRAM, FACEBOOK, BLOG, KAKAO, COMMUNITY
    
    @Column(name = "scheduled_date") // MySQL의 scheduled_date 컬럼과 매핑
    private LocalDateTime scheduledAt;
    
    @Column(name = "border_color", length = 20)
    private String borderColor; // UI 구분용 컬러코드 (예: #E1306C)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING) // DB에 "SCHEDULED" 문자열로 저장
    @Column(length = 20, nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.PENDING;

    // 데이터 저장 전 실행되는 훅 (생성일시 자동 주입)
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PostStatus.PENDING;
        }
    }
}