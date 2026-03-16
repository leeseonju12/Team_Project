package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record NaverKeywordDto(
    int    rankNo,
    String keywordText,
    int    searchCount
) {}
