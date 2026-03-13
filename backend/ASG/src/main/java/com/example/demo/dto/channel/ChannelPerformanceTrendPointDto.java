package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record ChannelPerformanceTrendPointDto(
        int dateKey,
        int views,
        int likes,
        int comments,
        int shares
) {
}