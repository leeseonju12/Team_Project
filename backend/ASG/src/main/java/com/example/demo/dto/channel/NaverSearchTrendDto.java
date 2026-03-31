package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record NaverSearchTrendDto(
    String label,       // 차트 x축 레이블 ("6월", "12/25" 등)
    int    searchCount  // 검색량 지수 * 100
) {}

/*

채널 성과 분석 페이지에서 쓰이고 있음
네이버 검색 영향 추적

*/