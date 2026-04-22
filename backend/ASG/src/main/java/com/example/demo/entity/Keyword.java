package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "keywords", indexes = {
    @Index(name = "idx_keyword_industry", columnList = "industryCode")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "industry_code", nullable = false, length = 50)
    private String industryCode;

    @Column(length = 50)
    private String category;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder
    public Keyword(String industryCode, String category, String name) {
        this.industryCode = industryCode;
        this.category = category;
        this.name = name;
    }
}