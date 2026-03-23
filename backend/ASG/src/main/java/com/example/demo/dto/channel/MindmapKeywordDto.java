package com.example.demo.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MindmapKeywordDto {

    private String keyword;   // 키워드 텍스트
    private String source;    // "GOOGLE" or "NAVER"
    private int position;     // 원래 순위
    private double score;        // 병합 점수 (낮을수록 높은 순위)
    private String googleUrl;  // 클릭 시 이동할 주소
    private String naverUrl; // 구글/네이버 각각으로 분리
}