package com.example.demo.service;

import com.example.demo.dto.channel.MindmapKeywordDto;
import com.example.demo.dto.channel.MindmapResponseDto;
import com.example.demo.dto.channel.SerpApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MindmapService {

    private final SerpApiClient serpApiClient;
    private final JdbcTemplate jdbcTemplate;  // brand 테이블에서 브랜드명 조회용

    /**
     * 연관 키워드 조회 (메인 메서드)
     * Redis 캐시 확인 → 없으면 SerpAPI 호출 → 병합 → 캐시 저장 → 반환
     * value = "related-keywords" 는 CacheConfig 에서 TTL 6개월로 설정한 캐시 이름
     * key = brandId 로 캐시 구분
     */
    @Cacheable(value = "related-keywords", key = "#brandId")
    public MindmapResponseDto getRelatedKeywords(Long brandId) {

        // 1. brand 테이블에서 브랜드명 조회
        Map<String, Object> brand = jdbcTemplate.queryForMap(
                "SELECT brand_name FROM brand WHERE brand_id = ?", brandId);
        String brandName = brand.get("brand_name").toString();

        // 2. 구글 + 네이버 병렬 호출 (동기 처리로 변환)
        SerpApiResponseDto[] results = serpApiClient
                .fetchBothRelatedKeywords(brandName)
                .block();  // WebFlux Mono → 일반 동기 방식으로 변환

        SerpApiResponseDto googleResult = results[0];
        SerpApiResponseDto naverResult  = results[1];

        // 3. 구글 + 네이버 결과 병합 및 점수 계산
        List<MindmapKeywordDto> merged = mergeAndScore(googleResult, naverResult);

        // 4. 최종 응답 반환 (캐시에 자동 저장됨)
        return new MindmapResponseDto(
                brandId,
                brandName,
                merged.size(),
                merged
        );
    }

    /**
     * 캐시 강제 갱신 (관리자용)
     * Redis 에서 해당 brandId 캐시 삭제 → 재조회
     */
    @CacheEvict(value = "related-keywords", key = "#brandId")
    public MindmapResponseDto refreshRelatedKeywords(Long brandId) {
        return getRelatedKeywords(brandId);  // 캐시 삭제 후 재조회
    }

    /**
     * 구글 + 네이버 결과 병합 및 점수 계산 (백분위 기반)
     *
     * 점수 계산 방식:
     * - BOTH  → 구글백분위 + 네이버백분위 (낮을수록 상위)
     * - GOOGLE만 → 구글백분위 + 0.5 페널티
     * - NAVER만  → 네이버백분위 + 0.5 페널티
     */
    private List<MindmapKeywordDto> mergeAndScore(
            SerpApiResponseDto googleResult,
            SerpApiResponseDto naverResult) {

        // 구글 전체 개수, 네이버 전체 개수 먼저 구하기
        int googleTotal = (googleResult.getRelatedSearches() != null) ? googleResult.getRelatedSearches().size() : 1;
        int naverTotal  = (naverResult.getRelatedResults()   != null) ? naverResult.getRelatedResults().size()   : 1;

        // 키워드별 [구글순위, 네이버순위] 저장 (-1 = 해당 엔진에 없음)
        Map<String, double[]> scoreMap = new LinkedHashMap<>();

        // 구글 결과 처리 (배열 인덱스 기반 순위)
        if (googleResult.getRelatedSearches() != null) {
            List<SerpApiResponseDto.GoogleRelatedSearch> googleList = googleResult.getRelatedSearches();
            for (int idx = 0; idx < googleList.size(); idx++) {
                String keyword = googleList.get(idx).getQuery().trim();
                scoreMap.put(keyword, new double[]{idx + 1, -1});
            }
        }

        // 네이버 결과 처리
        if (naverResult.getRelatedResults() != null) {
            for (SerpApiResponseDto.NaverRelatedResult item : naverResult.getRelatedResults()) {
                String keyword = item.getTitle().trim();
                if (scoreMap.containsKey(keyword)) {
                    // 양쪽 다 있는 키워드 → 네이버 순위 추가
                    scoreMap.get(keyword)[1] = item.getPosition();
                } else {
                    // 네이버에만 있는 키워드
                    scoreMap.put(keyword, new double[]{-1, item.getPosition()});
                }
            }
        }

        // 최종 점수 계산 (백분위 기반)
        List<MindmapKeywordDto> list = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : scoreMap.entrySet()) {
            String keyword   = entry.getKey();
            double googlePos = entry.getValue()[0];
            double naverPos  = entry.getValue()[1];

            // 백분위 변환 (-1 이면 해당 엔진에 없음)
            double googleScore = (googlePos != -1) ? googlePos / googleTotal : -1;
            double naverScore  = (naverPos  != -1) ? naverPos  / naverTotal  : -1;

            double finalScore;
            String source;

            if (googleScore != -1 && naverScore != -1) {
                // BOTH → 두 백분위 합산
                finalScore = googleScore + naverScore;
                source = "BOTH";
            } else if (googleScore != -1) {
                // 구글만 → 백분위 + 0.5 페널티
                finalScore = googleScore + 0.5;
                source = "GOOGLE";
            } else {
                // 네이버만 → 백분위 + 0.5 페널티
                finalScore = naverScore + 0.5;
                source = "NAVER";
            }

            String googleUrl = "https://www.google.com/search?q=" +
                    java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
            String naverUrl  = "https://search.naver.com/search.naver?query=" +
                    java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);

            int minPos = (int) Math.min(googlePos == -1 ? 999 : googlePos, naverPos == -1 ? 999 : naverPos);

            list.add(new MindmapKeywordDto(
                    keyword,
                    source,
                    minPos,
                    finalScore,
                    googleUrl,
                    naverUrl
            ));
        }

        // 점수 오름차순 정렬 (낮을수록 상위)
        list.sort(Comparator.comparingDouble(MindmapKeywordDto::getScore));

        return list;
    }
}