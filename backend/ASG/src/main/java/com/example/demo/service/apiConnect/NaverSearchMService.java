package com.example.demo.service.apiConnect;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
     * @return: 분석 서비스 규격에 맞게 List<String> 형태로 반환합니다.
     */
    public List<String> getNaverBlogData(BrandSearchRequestDto request) {
        List<String> responses = new ArrayList<>(); // 💡 결과를 담을 리스트 생성
        
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

            // 5. 결과 리스트에 추가 (KeywordAnalysisService 규격 맞춤)
            if (response.getBody() != null) {
                responses.add(response.getBody());
            }

        } catch (Exception e) {
            // 실패 시 에러 메시지를 포함한 JSON을 리스트에 추가
            responses.add("{\"error\":\"네이버 API 호출 실패: " + e.getMessage() + "\"}");
        }
        
        return responses; // 💡 최종적으로 리스트 반환
    }
}