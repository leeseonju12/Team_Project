package com.example.demo.service;

import com.example.demo.dto.channel.MindmapSearchKeywordDto;
import com.example.demo.dto.channel.MindmapSearchResponseDto;
import com.example.demo.dto.channel.SerpApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 연관 검색어 순위

*/

@Service
@RequiredArgsConstructor
public class MindmapSearchService {

    private final SerpApiClient serpApiClient;
    private final JdbcTemplate jdbcTemplate;  // brand 테이블에서 브랜드명 조회용

    /**
     * 연관 키워드 조회 (메인 메서드)
     * Redis 캐시 확인 → 없으면 SerpAPI 호출 → 병합 → 캐시 저장 → 반환
     * value = "related-keywords" 는 CacheConfig 에서 TTL 6개월로 설정한 캐시 이름
     * key = brandId 로 캐시 구분
     */
    @Cacheable(value = "related-keywords", key = "#brandId")
    public MindmapSearchResponseDto getRelatedKeywords(Long brandId) {

    	// 1. brand 테이블에서 브랜드명 조회
        Map<String, Object> brand = jdbcTemplate.queryForMap(
                "SELECT brand_name FROM brand WHERE brand_id = ?", brandId);
        String brandName   = brand.get("brand_name").toString();

        // 연관 검색어는 brand_name만 사용
        // service_name(인스타 계정명) 포함 시 네이버 연관검색어 미반환
        String searchQuery = brandName;

        // 2. 구글 + 네이버 병렬 호출 (동기 처리로 변환)
        SerpApiResponseDto[] results = serpApiClient
                .fetchBothRelatedKeywords(searchQuery)
                .block();  // WebFlux Mono → 일반 동기 방식으로 변환

        SerpApiResponseDto googleResult = results[0];
        SerpApiResponseDto naverResult  = results[1];

        // 디버그 로그
        System.out.println("====> [구글 연관검색어] " + 
            (googleResult.getRelatedSearches() != null ? googleResult.getRelatedSearches().size() + "개" : "null"));
        System.out.println("====> [네이버 연관검색어] " + 
            (naverResult.getRelatedResults() != null ? naverResult.getRelatedResults().size() + "개" : "null"));

        // 3. 구글 + 네이버 결과 병합 및 점수 계산
        List<MindmapSearchKeywordDto> merged = mergeAndScore(googleResult, naverResult);

        // 4. 최종 응답 반환 (캐시에 자동 저장됨)
        return new MindmapSearchResponseDto(
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
    public MindmapSearchResponseDto refreshRelatedKeywords(Long brandId) {
        return getRelatedKeywords(brandId);  // 캐시 삭제 후 재조회
    }

    /**
     * 구글 + 네이버 결과 병합 및 점수 계산 (백분위 기반)
     *
     * 전처리:
     * - 구글/네이버 각각 최대 8개로 제한 → 동일한 모수로 공정 비교
     *
     * 점수 계산 방식:
     * - BOTH   → 두 백분위 평균 (구글과 네이버 둘 다 인정한 키워드)
     * - GOOGLE → 구글 백분위 그대로
     * - NAVER  → 네이버 백분위 그대로
     *
     * 모수가 동일(8개)하므로 별도 페널티 없이 백분위만으로 공정하게 비교
     * BOTH는 두 백분위 평균이라 자연스럽게 유리하지만 무조건 앞에 오지는 않음
     * 낮은 점수일수록 상위
     */
    private List<MindmapSearchKeywordDto> mergeAndScore(
            SerpApiResponseDto googleResult,
            SerpApiResponseDto naverResult) {

        // ── 1. 구글/네이버 각각 8개로 제한 ──────────────────────────
        // 모수를 동일하게 맞춰서 백분위 비교를 공정하게 만든다
        List<SerpApiResponseDto.GoogleRelatedSearch> googleList =
            googleResult.getRelatedSearches() != null
                ? googleResult.getRelatedSearches().stream().limit(8).collect(java.util.stream.Collectors.toList())
                : new ArrayList<>();

        List<SerpApiResponseDto.NaverRelatedResult> naverList =
            naverResult.getRelatedResults() != null
                ? naverResult.getRelatedResults().stream().limit(8).collect(java.util.stream.Collectors.toList())
                : new ArrayList<>();

        // 0으로 나누기 방지 (결과가 없으면 1로 설정)
        int googleTotal = googleList.isEmpty() ? 1 : googleList.size();
        int naverTotal  = naverList.isEmpty()  ? 1 : naverList.size();

        // ── 2. 키워드별 [구글순위, 네이버순위] 임시 저장 ───────────────
        // -1 = 해당 엔진에 없음
        Map<String, double[]> scoreMap = new LinkedHashMap<>();

        // 구글 결과 처리 (배열 인덱스 기반 순위)
        for (int idx = 0; idx < googleList.size(); idx++) {
            String keyword = googleList.get(idx).getQuery().trim();
            scoreMap.put(keyword, new double[]{idx + 1, -1});
        }

        // 네이버 결과 처리
        // 이미 구글에 있는 키워드면 네이버 순위 추가 → BOTH
        // 없는 키워드면 새로 추가 → NAVER 단독
        for (SerpApiResponseDto.NaverRelatedResult item : naverList) {
            String keyword = item.getTitle().trim();
            if (scoreMap.containsKey(keyword)) {
                scoreMap.get(keyword)[1] = item.getPosition();
            } else {
                scoreMap.put(keyword, new double[]{-1, item.getPosition()});
            }
        }

        // ── 3. 백분위 변환 및 최종 점수 계산 ───────────────────────────
        List<MindmapSearchKeywordDto> list = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : scoreMap.entrySet()) {
            String keyword   = entry.getKey();
            double googlePos = entry.getValue()[0];
            double naverPos  = entry.getValue()[1];

            // 순위를 백분위로 변환 (-1이면 해당 엔진에 없는 키워드)
            double googleScore = (googlePos != -1) ? googlePos / googleTotal : -1;
            double naverScore  = (naverPos  != -1) ? naverPos  / naverTotal  : -1;

            double finalScore;
            String source;

            if (googleScore != -1 && naverScore != -1) {
                // BOTH → 두 백분위 평균
                // 두 플랫폼 모두 인정한 키워드로 자연스럽게 유리
                finalScore = (googleScore + naverScore) / 2.0;
                source = "BOTH";
            } else if (googleScore != -1) {
                // 구글 단독 → 백분위 그대로
                finalScore = googleScore;
                source = "GOOGLE";
            } else {
                // 네이버 단독 → 백분위 그대로
                finalScore = naverScore;
                source = "NAVER";
            }

            String googleUrl = "https://www.google.com/search?q=" +
                    java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
            String naverUrl  = "https://search.naver.com/search.naver?query=" +
                    java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);

            int minPos = (int) Math.min(
                    googlePos == -1 ? 999 : googlePos,
                    naverPos  == -1 ? 999 : naverPos);

            list.add(new MindmapSearchKeywordDto(keyword, source, minPos, finalScore, googleUrl, naverUrl));
        }

        // ── 4. 점수 오름차순 정렬 (낮을수록 상위) ──────────────────────
        list.sort(Comparator.comparingDouble(MindmapSearchKeywordDto::getScore));

        return list;
    }
}