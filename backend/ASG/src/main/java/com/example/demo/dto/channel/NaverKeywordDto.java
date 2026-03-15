package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record NaverKeywordDto(
        Long   keywordId,
        String keywordText,
        String keywordType,
        int    searchCount,
        int    clickCount,
        int    rankNo
) {}
