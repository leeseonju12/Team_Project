package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record NaverSearchTrendDto(
        int dateKey,
        int searchCount,
        int clickCount
) {}
