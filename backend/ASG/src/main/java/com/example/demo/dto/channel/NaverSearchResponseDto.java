package com.example.demo.dto.channel;

import java.util.List;
import lombok.Builder;

@Builder
public record NaverSearchResponseDto(
    NaverSearchSummaryDto     summary,
    List<NaverSearchTrendDto> trends,
    List<NaverKeywordDto>     keywords,
    // 연령대
    List<String>  ageLabels,
    List<Integer> ageCur,
    List<Integer> agePrev,
    // 성별
    int genderFemaleCur,
    int genderMaleCur,
    int genderFemalePrev,
    int genderMalePrev
) {}

/*

채널 성과 분석 페이지에서 쓰이고 있음
네이버 검색 영향 추적

*/

