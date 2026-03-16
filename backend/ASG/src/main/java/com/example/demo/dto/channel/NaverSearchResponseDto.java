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
