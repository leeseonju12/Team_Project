package com.example.demo.dto.channel;
import java.util.List;

import lombok.Builder;

@Builder
public record ChannelPerformanceResponseDto(
        Long brandId,
        String from,
        String to,
        List<ChannelPerformanceSummaryDto> summaries,
        List<ChannelPerformanceTrendPointDto> trends,
        List<ChannelTopPostDto> topPosts,
        ChannelPerformanceInsightDto insight
) {
}