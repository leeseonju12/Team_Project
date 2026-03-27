package com.example.demo.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class PlatformKeywordResponseDto {
    private String brand;
    private List<String> instagramKeywords; // 프론트 변수명과 맞출 것
    private List<String> youtubeKeywords;
    private List<String> naverKeywords;
    private List<String> googleKeywords;
}

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 차트

*/