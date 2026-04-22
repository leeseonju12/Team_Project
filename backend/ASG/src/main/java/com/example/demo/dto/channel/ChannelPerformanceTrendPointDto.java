package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record ChannelPerformanceTrendPointDto(
        int dateKey,
        int likes,
        int comments,
        int shares,
        int reviews
) {
}