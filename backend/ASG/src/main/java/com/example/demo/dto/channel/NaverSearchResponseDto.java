package com.example.demo.dto.channel;

import java.util.List;
import lombok.Builder;

@Builder
public record NaverSearchResponseDto(
        NaverSearchSummaryDto    summary,
        List<NaverSearchTrendDto> trends,
        List<NaverKeywordDto>    keywords
) {}
