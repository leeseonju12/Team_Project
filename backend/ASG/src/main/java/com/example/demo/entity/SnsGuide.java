package com.example.demo.entity;


import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sns_guides")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SnsGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String platform;

 // Rationale: 문구와 시간을 1:1로 강제 매핑하기 위해 단일 JSON 객체 배열로 구조를 변경합니다.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "guide_details", columnDefinition = "json")
    private List<GuideDetail> guideDetails;

    @Builder
    public SnsGuide(String platform, List<GuideDetail> guideDetails) {
        this.platform = platform;
        this.guideDetails = guideDetails;
    }

    // Rationale: JSON 역직렬화를 위해 기본 생성자와 Getter가 필수적으로 요구됩니다.
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuideDetail implements Serializable {
        private String content;
        private String bestTime;
    }

}