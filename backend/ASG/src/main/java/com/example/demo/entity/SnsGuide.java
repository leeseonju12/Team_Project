package com.example.demo.entity;


import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    // 여러 가이드 문구를 JSON 배열로 저장
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contents", columnDefinition = "json")
    private List<String> contents;

    // 여러 추천 시간을 JSON 배열로 저장
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "best_times", columnDefinition = "json")
    private List<String> bestTimes;

    @Builder
    public SnsGuide(String platform, List<String> contents, List<String> bestTimes) {
        this.platform = platform;
        this.contents = contents;
        this.bestTimes = bestTimes;
    }
}