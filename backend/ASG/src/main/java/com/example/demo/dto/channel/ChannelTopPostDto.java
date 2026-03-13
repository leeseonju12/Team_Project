package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record ChannelTopPostDto(
        Long postId,
        String postTitle,
        String platformName,
        int views,
        int likes,
        int comments,
        int shares,
        double engagementRate
) {
}