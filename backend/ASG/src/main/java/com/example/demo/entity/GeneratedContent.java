package com.example.demo.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.demo.domain.calendar.entity.ContentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "generated_content") // 테이블명 수정
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GeneratedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_name")
    private String menuName; // title -> menuName 변경

    @Column(columnDefinition = "TEXT")
    private String content;

    private String hashtags; // 추가

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ContentStatus status = ContentStatus.PENDING;

    @Column(name = "image_url")
    private String imageUrl; // 추가

    @Column(name = "origin_url")
    private String originUrl; // 추가

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate; // 필드명 통일

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 추가

    @CreationTimestamp // @PrePersist 대신 하이버네이트 어노테이션 권장
    @Column(name = "created_at", updatable = false, precision = 6)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 수정 시간 자동 관리
    @Column(name = "updated_at", precision = 6)
    private LocalDateTime updatedAt;
    
 // GeneratedContent 엔티티 내부에 추가
    public void updateSchedule(LocalDateTime scheduledDate, ContentStatus status) {
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.publishedAt = scheduledDate; 
    }
}