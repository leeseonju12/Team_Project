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

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 차트

*/

@Service
@RequiredArgsConstructor
public class PlatformKeywordService {
 
    private final GoogleSearchService googleSearchService;
    private final YoutubeService youtubeService;
    private final NaverSearchMService naverSearchMService;
    private final KeywordAnalysisService keywordAnalysisService;
    private final JdbcTemplate jdbcTemplate;  // 추가
 
    public PlatformKeywordResponseDto getKeywords(Long brandId) {
 
        // 1. brandId로 브랜드명 + 서비스명 조회
        Map<String, Object> brand = jdbcTemplate.queryForMap(
                "SELECT brand_name, service_name FROM brand WHERE brand_id = ?", brandId);
        String brandName   = brand.get("brand_name").toString();
        String serviceName = brand.get("service_name") != null ? brand.get("service_name").toString() : "";
        // service_name이 브랜드명과 다를 때만 사용 (더 구체적인 검색어)
        // 예: brand_name="그라운드요가", service_name="그라운드요가 스튜디오" → "그라운드요가 스튜디오" 로 검색
        String searchQuery = (serviceName.isBlank() || serviceName.equals(brandName))
                           ? brandName : serviceName;

        // 2. DTO 생성
        BrandSearchRequestDto request = new BrandSearchRequestDto(searchQuery, "month");
 
        // 3. 각 플랫폼 데이터 수집
        List<String> instagramRaw = googleSearchService.getGoogleData(request, true);
        List<String> googleRaw    = googleSearchService.getGoogleData(request, false);
        List<String> youtubeRaw   = youtubeService.getYoutubeData(request);
        List<String>       naverRaw     = naverSearchMService.getNaverBlogData(request);
 
        // 4. n-gram 키워드 분석 (brandName을 동적 불용어로 전달)
        List<String> instagramKeywords = keywordAnalysisService.analyzeKeywords(instagramRaw, brandName);
        List<String> googleKeywords    = keywordAnalysisService.analyzeKeywords(googleRaw,    brandName);
        List<String> youtubeKeywords   = keywordAnalysisService.analyzeKeywords(youtubeRaw,   brandName);
        List<String> naverKeywords     = keywordAnalysisService.analyzeKeywords(naverRaw,     brandName);
 
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