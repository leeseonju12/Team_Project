package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record NaverSearchSummaryDto(
    int    totalSearchCount,    // 기간 내 검색 활동 지수 평균
    double searchGrowthPct,     // 전기간 대비 증감률(%)
    String topAgeGroup,         // 예: "30대" (차트용 유지)
    String topGender,           // 예: "여성" (차트용 유지)
    String searchActivityStatus,// "폭발적" / "안정적" / "침체기"
    String topUser,             // 예: "30대 여성" — 나이+성별 조합 중 가장 높은 검색 그룹
    String prevTopUser          // 이전 기간 최고 검색 그룹
) {}