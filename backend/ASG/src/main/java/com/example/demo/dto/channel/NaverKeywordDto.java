package com.example.demo.dto.channel;

import lombok.Builder;

@Builder
public record NaverKeywordDto(
    int    rankNo,
    String keywordText,
    int    searchCount
) {}

/*

채널 성과 분석 페이지에서 쓰이고 있음
네이버 검색 영향 추적

*/

