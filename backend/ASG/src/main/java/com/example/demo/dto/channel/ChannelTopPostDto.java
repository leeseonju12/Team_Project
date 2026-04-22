package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record ChannelTopPostDto(
        Long postId,
        String postTitle,
        String platformName,
        int likes,
        int comments,
        int shares,
        int reviewCount,
        double engagementRate
) {
}