package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

 // 🌟 1. page 파라미터를 추가로 받습니다. (기본값은 1)
    @GetMapping("/search")
    public ResponseEntity<?> searchImages(@RequestParam String query, 
                                          @RequestParam(defaultValue = "1") int page) {
        try {
            String englishQuery = translateKoreanToEnglish(query);
            System.out.println("검색어: " + query + " -> 번역: " + englishQuery + " (페이지: " + page + ")");

            RestTemplate restTemplate = new RestTemplate();
            // 🌟 2. per_page를 30으로 넉넉하게 늘리고, page 변수를 URL에 끼워 넣습니다.
            String pexelsUrl = "https://api.pexels.com/v1/search?query=" + englishQuery + "&per_page=12&page=" + page + "&locale=en-US";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", pexelsApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(pexelsUrl, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("검색 오류: " + e.getMessage());
        }
    }

 // 🛠️ MyMemory API를 호출하는 완전 무료 번역 도우미 메서드 (카드 등록, API 키 전혀 필요 없음!)
    private String translateKoreanToEnglish(String text) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // 🌟 한글 검색어에 띄어쓰기가 있어도 에러가 안 나도록 URL 템플릿 변수({query}) 사용
            String myMemoryUrl = "https://api.mymemory.translated.net/get?q={query}&langpair=ko|en";

            // GET 요청 쏘기 (인증 헤더나 키 세팅이 1도 필요 없습니다)
            ResponseEntity<String> response = restTemplate.getForEntity(myMemoryUrl, String.class, text);

            // JSON 결과 파싱 (Jackson ObjectMapper 사용)
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            
            // MyMemory 응답 구조에서 번역된 텍스트만 쏙 뽑아내기
            return root.path("responseData").path("translatedText").asText();

        } catch (Exception e) {
            System.err.println("번역 API 호출 실패. 원본 검색어로 진행합니다: " + e.getMessage());
            return text; 
        }
    }
}