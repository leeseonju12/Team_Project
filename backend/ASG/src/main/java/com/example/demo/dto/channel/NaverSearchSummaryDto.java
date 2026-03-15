package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record NaverSearchSummaryDto(
        int    totalSearchCount,
        int    totalClickCount,
        double searchGrowthPct,   // 전 기간 대비 검색수 증감률(%)
        double clickGrowthPct     // 전 기간 대비 클릭수 증감률(%)
) {}
