package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/sns/pexels")
public class PexelsController {

    @Value("${pexels.api.key}")
    private String pexelsApiKey;

    /* Rationale: page 파라미터를 추가 수신 (기본값 1). per_page 설정 확장을 통해 검색 결과 다양성 확보 */
    @GetMapping("/search")
    public ResponseEntity<?> searchImages(@RequestParam String query, 
                                          @RequestParam(defaultValue = "1") int page) {
        try {
            String englishQuery = translateKoreanToEnglish(query);
            System.out.println("검색어: " + query + " -> 번역: " + englishQuery + " (페이지: " + page + ")");
            RestTemplate restTemplate = new RestTemplate();
            
            String pexelsUrl = "https://api.pexels.com/v1/search?query=" + englishQuery + "&per_page=12&page=" + page + "&locale=en-US";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", pexelsApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(pexelsUrl, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("검색 오류: " + e.getMessage());
        }
    }

    /* Rationale: MyMemory API를 활용한 서버사이드 한영 번역. 띄어쓰기 URL 인코딩 이슈를 방지하기 위해 템플릿 변수({query}) 사용 */
    private String translateKoreanToEnglish(String text) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String myMemoryUrl = "https://api.mymemory.translated.net/get?q={query}&langpair=ko|en";

            ResponseEntity<String> response = restTemplate.getForEntity(myMemoryUrl, String.class, text);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            
            return root.path("responseData").path("translatedText").asText();

        } catch (Exception e) {
            System.err.println("번역 API 호출 실패. 원본 검색어로 진행합니다: " + e.getMessage());
            return text; 
        }
    }
}