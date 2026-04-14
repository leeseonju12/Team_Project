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
    private final JdbcTemplate jdbcTemplate;

    public PlatformKeywordResponseDto getKeywords(Long brandId) {

        // 1. brandId로 브랜드명 + 위치명 + 서비스명 조회
        Map<String, Object> brand = jdbcTemplate.queryForMap(
                "SELECT brand_name, address, service_name FROM brand WHERE brand_id = ?", brandId);
        String brandName    = brand.get("brand_name").toString();
        String locationName = brand.get("address") != null ? brand.get("address").toString() : "";
        String serviceName  = brand.get("service_name")  != null ? brand.get("service_name").toString()  : "";

        // 공통 검색어: brand_name + address + service_name
        // 예: "잉글리쉬가든 강원 춘천시 englishgarden_21"
        String searchQuery = brandName
                + (locationName.isBlank() ? "" : " " + locationName)
                + (serviceName.isBlank()  ? "" : " " + serviceName);
        BrandSearchRequestDto request = new BrandSearchRequestDto(searchQuery, "year");

        // 유튜브/네이버 전용: brand_name만 사용
        // - 유튜브: 검색어가 길면 결과가 적어짐
        // - 네이버 블로그: 계정명 포함 시 관련 없는 결과 유입
        BrandSearchRequestDto brandOnlyRequest = new BrandSearchRequestDto(brandName, "year");

        // 2. 각 플랫폼 데이터 수집
        List<String> instagramRaw = googleSearchService.getGoogleData(request,          true);
        List<String> googleRaw    = googleSearchService.getGoogleData(request,          false);
        List<String> youtubeRaw   = youtubeService.getYoutubeData(brandOnlyRequest);
        List<String> naverRaw     = naverSearchMService.getNaverBlogData(brandOnlyRequest);

        // 3. 키워드 분석 (brandName을 동적 불용어로 전달)
        List<String> instagramKeywords = keywordAnalysisService.analyzeKeywords(instagramRaw, brandName);
        List<String> googleKeywords    = keywordAnalysisService.analyzeKeywords(googleRaw,    brandName);
        List<String> youtubeKeywords   = keywordAnalysisService.analyzeKeywords(youtubeRaw,   brandName);
        List<String> naverKeywords     = keywordAnalysisService.analyzeKeywords(naverRaw,     brandName);

        return new PlatformKeywordResponseDto(
                brandName,
                instagramKeywords,
                youtubeKeywords,
                naverKeywords,
                googleKeywords
        );
    }
}