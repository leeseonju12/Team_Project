package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component // 또는 @Service
//@RequiredArgsConstructor
public class GeminiApiClient {
    
    @Value("${ai.api-key}")
    private String geminiApiKey;
    private final RestTemplate restTemplate = new RestTemplate(); //나중에 bean할예정
    private final ObjectMapper objectMapper = new ObjectMapper(); // 얘도

    public String requestToGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + geminiApiKey;

    	
        // 1. 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. 바디(요청 JSON) 생성 (Map 활용)
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> partNode = new HashMap<>();
        partNode.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(partNode));

        // 3. HTTP 요청 엔티티 생성
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 4. API 호출
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            
            // 5. 응답 JSON에서 실제 답변 텍스트만 쏙 뽑아내기
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.path("candidates")
                           .get(0)
                           .path("content")
                           .path("parts")
                           .get(0)
                           .path("text")
                           .asText();
                           
        } catch (Exception e) {
            System.err.println("Gemini API 호출 중 에러: " + e.getMessage());
            return "[]"; // 에러 시 빈 배열을 반환하여 화면 렌더링이 터지지 않도록 보호
        }
    }
}