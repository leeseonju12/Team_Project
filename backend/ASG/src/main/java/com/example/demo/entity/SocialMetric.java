package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class SocialMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🌟 핵심 추가: 이 통계의 주인이 누구인지 식별 (User 엔티티와 매핑하거나 ID만 저장)
    @Column(nullable = false)
    private Long userId;

    private String platform;
    private String targetType;    // "PAGE" (계정 단위) 또는 "POST" (게시물 단위)
    private String targetId;      // 페이지 ID 또는 게시물 ID

    private LocalDate recordDate; // 통계 수집 날짜

    private int likesCount;       
    private int commentsCount;    
    private int sharesCount;      
    private Integer viewsCount;       
    private int newFollowers;     
}