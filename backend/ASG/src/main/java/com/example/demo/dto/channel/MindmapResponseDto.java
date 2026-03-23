package com.example.demo.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MindmapResponseDto {

    private Long brandId;
    private String brandName;
    private int totalCount;                  // 키워드 총 개수 (프론트 뱃지용)
    private List<MindmapKeywordDto> keywords; // 순위 정렬된 키워드 리스트
}