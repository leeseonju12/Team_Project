package com.example.demo.service.apiConnect;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSearchService {

    @Value("${google.api-key}")
    private String apiKey;

    @Value("${google.cx}")
    private String cx;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * @param isInstagram true면 인스타그램만, false면 구글 웹 전체를 검색합니다.
     */
    public List<String> getGoogleData(BrandSearchRequestDto request, boolean isInstagram) {
        List<String> responses = new ArrayList<>(); 
        
        // 1. [인스타 분리 핵심] 검색어 설정
        String query = request.getBrandName();
        
        // ⭐ 이 부분에서 인스타그램 데이터만 따로 필터링하도록 검색어를 변조합니다.
        if (isInstagram) {
            // site:instagram.com 키워드를 붙이면 구글이 인스타그램 게시물만 수집해옵니다.
            query = "site:instagram.com " + query; 
        }

        // 2. 기간 설정 (사용자가 선택한 week, month, year 적용)
        String dateRestrict = convertPeriod(request.getPeriod());

        // 3. 100개 수집을 위한 반복문 (10개씩 10번 호출)
        for (int i = 1; i <= 91; i += 10) {
            String apiURL = UriComponentsBuilder.fromUriString("https://www.googleapis.com/customsearch/v1")
                    .queryParam("key", apiKey)
                    .queryParam("cx", cx)
                    .queryParam("q", query) // 위에서 설정된 인스타 전용 쿼리가 들어갑니다.
                    .queryParam("dateRestrict", dateRestrict)
                    .queryParam("num", 10)
                    .queryParam("start", i) // 다음 페이지 시작 위치
                    .build().toUriString();

            try {
                // 수집된 JSON 데이터를 리스트에 차곡차곡 담습니다.
                ResponseEntity<String> response = restTemplate.getForEntity(apiURL, String.class);
                responses.add(response.getBody());
            } catch (Exception e) {
                // 실패 시 에러 내용을 담아 분석 단계에서 제외할 수 있게 합니다.
                responses.add("{\"error\":\"인덱스 " + i + " 수집 실패\"}");
            }
        }
        return responses; 
    }

    /**
     * 기간 토글 변환 메서드
     */
    private String convertPeriod(String period) {
        if (period == null) return "m1"; 
        return switch (period) {
            case "week" -> "d7";   // 1주일 이내 게시물
            case "month" -> "m1";  // 1달 이내 게시물
            case "year" -> "y1";   // 1년 이내 게시물
            default -> "m1";
        };
    }
}