package com.example.demo.service;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import com.example.demo.dto.channel.PlatformKeywordResponseDto;
import com.example.demo.service.apiConnect.GoogleSearchService;
import com.example.demo.service.apiConnect.NaverSearchMService;
import com.example.demo.service.apiConnect.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformKeywordService {

    private final GoogleSearchService googleSearchService;
    private final YoutubeService youtubeService;
    private final NaverSearchMService naverSearchMService;
    private final KeywordAnalysisService keywordAnalysisService;

    public PlatformKeywordResponseDto getKeywords(BrandSearchRequestDto request) {

        // 1. 각 플랫폼 데이터 수집
        List<String> instagramRaw = googleSearchService.getGoogleData(request, true);
        List<String> googleRaw    = googleSearchService.getGoogleData(request, false);
        List<String> youtubeRaw   = youtubeService.getYoutubeData(request);
        String       naverRaw     = naverSearchMService.getNaverBlogData(request);

        // 2. Komoran 키워드 분석 (Naver는 List로 감싸서 전달)
        List<String> instagramKeywords = keywordAnalysisService.analyzeKeywords(instagramRaw);
        List<String> googleKeywords    = keywordAnalysisService.analyzeKeywords(googleRaw);
        List<String> youtubeKeywords   = keywordAnalysisService.analyzeKeywords(youtubeRaw);
        List<String> naverKeywords     = keywordAnalysisService.analyzeKeywords(List.of(naverRaw));

        // 3. 응답 반환
        return new PlatformKeywordResponseDto(
                request.getBrandName(),
                instagramKeywords,
                youtubeKeywords,
                naverKeywords,
                googleKeywords
        );
    }
}