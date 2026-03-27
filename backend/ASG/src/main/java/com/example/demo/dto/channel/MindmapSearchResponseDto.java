package com.example.demo.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MindmapSearchResponseDto {

    private Long brandId;
    private String brandName;
    private int totalCount;                  // 키워드 총 개수 (프론트 뱃지용)
    private List<MindmapSearchKeywordDto> keywords; // 순위 정렬된 키워드 리스트
}

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 - 연관 검색어 부분

*/