package com.example.demo.service;

import com.example.demo.dto.channel.SerpApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 연관 검색어
구글이랑 네이버에서 검색어 가져오고있음

*/

@Component
@RequiredArgsConstructor
public class SerpApiClient {

    private final WebClient serpApiWebClient;

    @Value("${serpapi.api-key}")
    private String apiKey;

    /**
     * 구글 연관검색어 조회
     * SerpAPI Google 엔진 호출 → related_searches 파싱
     */
    public Mono<SerpApiResponseDto> fetchGoogleRelatedKeywords(String brandName) {
        return serpApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("engine", "google")
                        .queryParam("q", brandName)
                        .queryParam("hl", "ko")           // 한국어 결과
                        .queryParam("gl", "kr")           // 한국 지역 기준
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(SerpApiResponseDto.class);   // 응답 JSON → DTO 자동 파싱
    }

    /**
     * 네이버 연관검색어 조회
     * SerpAPI Naver 엔진 호출 → related_results 파싱
     */
    public Mono<SerpApiResponseDto> fetchNaverRelatedKeywords(String brandName) {
        return serpApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("engine", "naver")
                        .queryParam("query", brandName)  // 네이버는 q 대신 query 파라미터 사용
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(SerpApiResponseDto.class);
    }

    /**
     * 구글 + 네이버 동시 호출 (병렬)
     * Mono.zip 으로 두 요청을 동시에 날려서 응답 시간 단축
     * 결과는 SerpApiResponseDto 배열로 반환 [0] = 구글, [1] = 네이버
     */
    public Mono<SerpApiResponseDto[]> fetchBothRelatedKeywords(String brandName) {
        Mono<SerpApiResponseDto> googleMono = fetchGoogleRelatedKeywords(brandName);
        Mono<SerpApiResponseDto> naverMono  = fetchNaverRelatedKeywords(brandName);

        return Mono.zip(googleMono, naverMono)
                .map(tuple -> new SerpApiResponseDto[]{
                        tuple.getT1(), // [0] 구글 응답
                        tuple.getT2()  // [1] 네이버 응답
                });
    }
}