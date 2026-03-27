package com.example.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SnsResult {
    private String platform;      // instagram, naver 등 (ID용)
    private String platformAbbr;  // ig, nv 등 (CSS 클래스용)
    private String platformName;  // Instagram, 네이버 블로그 등
    private String content;       // AI 생성 문구
    private List<String> hashtags;// 해시태그 리스트
    private String guideText;     // 가이드 문구
    private String bestTime;      // 최적 게시 시간
    private String color;         // 포인트 컬러 코드 (#E1306C 등)
}