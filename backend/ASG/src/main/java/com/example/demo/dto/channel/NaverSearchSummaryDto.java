package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record NaverSearchSummaryDto(
    int    totalSearchCount,   // 기간 내 검색수 합계
    double searchGrowthPct,    // 전기간 대비 증감률(%)
    String topAgeGroup,        // 예: "20-30대"
    String topGender,           // 예: "여성"
    String searchActivityStatus // "폭발적" / "안정적" / "침체기"
) {}
