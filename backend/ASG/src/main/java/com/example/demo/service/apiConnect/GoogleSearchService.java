package com.example.demo.service.apiConnect;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleSearchService {

    @Value("${serpapi.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getGoogleData(BrandSearchRequestDto request, boolean isInstagram) {
        List<String> snippets = new ArrayList<>();
        
        // 1. 검색어 설정 (인스타그램 여부)
        String query = isInstagram ? "site:instagram.com " + request.getBrandName() : request.getBrandName();

        // 2. SerpApi URL 생성
        String apiURL = UriComponentsBuilder.fromUriString("https://serpapi.com/search")
                .queryParam("engine", "google")
                .queryParam("q", query)
                .queryParam("api_key", apiKey)
                .queryParam("num", 50) 
                .queryParam("google_domain", "google.co.kr")
                .queryParam("hl", "ko")
                .build().toUriString();

        try {
            // 3. API 호출
            Map<String, Object> response = restTemplate.getForObject(apiURL, Map.class);
            
            if (response != null && response.get("organic_results") != null) {
                // organic_results는 List<Map> 형태입니다.
                List<Object> results = (List<Object>) response.get("organic_results");
                
                for (Object obj : results) {
                    Map<String, Object> result = (Map<String, Object>) obj;
                    
                    // snippet 키가 있는지 확인하고 리스트에 추가
                    if (result.get("snippet") != null) {
                        String snippetText = result.get("snippet").toString();
                        snippets.add(snippetText);
                    }
                }
                System.out.println("====> [" + (isInstagram ? "인스타" : "구글") + "] 리스트 저장 완료: " + snippets.size() + "건");
            } else {
                System.out.println("====> [" + (isInstagram ? "인스타" : "구글") + "] organic_results 항목 없음");
            }
        } catch (Exception e) {
            System.err.println("====> API 호출 에러: " + e.getMessage());
            e.printStackTrace();
        }

        // 4. 수집된 문장 리스트 반환
        return snippets;
    }
}