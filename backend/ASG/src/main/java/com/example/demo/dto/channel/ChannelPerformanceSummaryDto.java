package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record ChannelPerformanceSummaryDto(
        Long brandPlatformId,
        String platformCode,
        String platformName,
        int totalViews,
        int totalLikes,
        int totalComments,
        int totalShares,
        int followerGrowth,
        double engagementRate
) {
}