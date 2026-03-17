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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeminiApiClient {
    
    @Value("${ai.api-key}")
    private String geminiApiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String requestToGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> partNode = new HashMap<>();
        partNode.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(partNode));
        
        requestBody.put("generationConfig", Map.of(
        	    "temperature", 1.5 // 0.0 ~ 2.0 사이값 (높을수록 창의적)
        	));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // --- 재시도 로직 적용 ---
        int maxRetry = 1;
        int retryCount = 0;

        while (retryCount <= maxRetry) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
                
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                return rootNode.path("candidates")
                               .get(0)
                               .path("content")
                               .path("parts")
                               .get(0)
                               .path("text")
                               .asText();
                               
            } catch (HttpServerErrorException.ServiceUnavailable e) {
                if (retryCount < maxRetry) {
                    retryCount++;
                    System.out.println("⚠️ Gemini 서버 과부하(503)! 2초 후 재시도합니다... (" + retryCount + "/1)");
                    try {
                        Thread.sleep(2000); // 🌟 InterruptedException 예외 처리
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue; // 다시 시도
                }
                System.err.println("❌ 재시도 횟수 초과 (503)");
                return "[]"; // 재시도 끝내고 실패 시 빈 배열 반환

            } catch (Exception e) {
                System.err.println("❌ Gemini API 호출 중 기타 에러: " + e.getMessage());
                return "[]"; // 🌟 모든 경로에서 반드시 String을 리턴하도록 보장
            }
        }
        
        return "[]"; // 🌟 자바 컴파일러를 위한 최종 안전장치 리턴
    }
}