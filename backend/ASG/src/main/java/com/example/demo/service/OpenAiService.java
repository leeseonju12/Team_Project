package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenAiService {

@Value("${openai.api-key}")
private String apiKey;

@Value("${openai.model:gpt-4o-mini}")
private String model;

@Value("${openai.max-tokens:500}")
private int maxTokens;

private final RestTemplate restTemplate = new RestTemplate();
private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

public String generateReply(String authorName, String reviewText) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    String prompt = """
            고객 이름: %s
            고객 리뷰: %s
            
            위 리뷰에 대해 정중하고 친근한 사장님 입장의 답변을 한국어로 3~5문장으로 작성.
            """.formatted(authorName, reviewText);

    Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", maxTokens,
            "messages", List.of(
                    Map.of("role", "system", "content", "당신은 친절한 가게 사장님입니다. 고객 리뷰에 진심 어린 답변을 작성합니다."),
                    Map.of("role", "user", "content", prompt)
            )
    );

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    try {
        ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, request, Map.class);
        List<Map> choices = (List<Map>) response.getBody().get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");

    } catch (Exception e) {
        log.error("OpenAI API 호출 실패 - feedbackAuthor: {}, error: {}", authorName, e.getMessage());
        throw new RuntimeException("AI 답변 생성에 실패했습니다.", e);
    }
}
}