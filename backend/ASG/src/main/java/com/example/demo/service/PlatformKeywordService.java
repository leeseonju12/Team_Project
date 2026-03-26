package com.example.demo.service;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import com.example.demo.dto.channel.PlatformKeywordResponseDto;
import com.example.demo.service.apiConnect.GoogleSearchService;
import com.example.demo.service.apiConnect.NaverSearchMService;
import com.example.demo.service.apiConnect.YoutubeService;
import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlatformKeywordService {

    private final GoogleSearchService googleSearchService;
    private final YoutubeService youtubeService;
    private final NaverSearchMService naverSearchMService;
    private final KeywordAnalysisService keywordAnalysisService;
    private final JdbcTemplate jdbcTemplate;  // 추가

    public PlatformKeywordResponseDto getKeywords(Long brandId) {

        // 1. brandId로 브랜드명 조회 (MindmapService와 동일한 방식)
        Map<String, Object> brand = jdbcTemplate.queryForMap(
                "SELECT brand_name FROM brand WHERE brand_id = ?", brandId);
        String brandName = brand.get("brand_name").toString();

        // 2. DTO 생성
        BrandSearchRequestDto request = new BrandSearchRequestDto(brandName, "month");

        // 3. 각 플랫폼 데이터 수집
        List<String> instagramRaw = googleSearchService.getGoogleData(request, true);
        List<String> googleRaw    = googleSearchService.getGoogleData(request, false);
        List<String> youtubeRaw   = youtubeService.getYoutubeData(request);
        List<String>       naverRaw     = naverSearchMService.getNaverBlogData(request);

        // 4. Komoran 키워드 분석
        List<String> instagramKeywords = keywordAnalysisService.analyzeKeywords(instagramRaw);
        List<String> googleKeywords    = keywordAnalysisService.analyzeKeywords(googleRaw);
        List<String> youtubeKeywords   = keywordAnalysisService.analyzeKeywords(youtubeRaw);
        List<String> naverKeywords     = keywordAnalysisService.analyzeKeywords(naverRaw);

     // PlatformKeywordService.java
        return new PlatformKeywordResponseDto(
                brandName,
                instagramKeywords, // 인스타
                youtubeKeywords,   // 유튜브
                naverKeywords,     // 네이버
                googleKeywords     // 구글
        );
    }
}