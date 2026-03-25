package com.example.demo.service.apiConnect;

import com.example.demo.dto.channel.BrandSearchRequestDto;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor // final 필드 생성자 주입
public class NaverSearchMService {

    // 💡 yml에 저장된 키 값을 가져옵니다.
    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * @param request: DTO를 통해 브랜드명과 기간 정보를 받습니다.
     */
    public String getNaverBlogData(BrandSearchRequestDto request) {
        
        // 1. 검색어 인코딩 (brand_name 사용)
        String encodedQuery = URLEncoder.encode(request.getBrandName(), StandardCharsets.UTF_8);

        // 2. URL 설정 (최신순 sort=date, 최대치 display=100)
        // 네이버 검색 API는 기간 필터 파라미터가 없으므로 최신순으로 많이 가져와서 서버에서 걸러야 합니다.
        String apiURL = "https://openapi.naver.com/v1/search/blog?query=" + encodedQuery 
                        + "&display=100&sort=date";

        // 3. 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 4. API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(apiURL),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // 5. 결과 반환 (이후 KeywordAnalysisService에서 기간별 필터링 진행)
            return response.getBody();

        } catch (Exception e) {
            // 실제 서비스에서는 로그를 남기는 것이 좋습니다.
            return "{\"error\":\"API 호출 실패\"}";
        }
    }
}