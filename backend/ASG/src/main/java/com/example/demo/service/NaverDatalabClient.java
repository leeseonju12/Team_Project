package com.example.demo.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.dto.channel.NaverDatalabRequestDto;
import com.example.demo.dto.channel.NaverDatalabResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NaverDatalabClient {

    private static final Logger log = LoggerFactory.getLogger(NaverDatalabClient.class);

    // HTTP 클라이언트를 재사용 (매 호출마다 생성 X → 연결 오버헤드 감소)
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))   // 연결 타임아웃: 5초
            .build();

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    @Value("${naver.api.datalab-url}")
    private String datalabUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public NaverDatalabResponseDto search(NaverDatalabRequestDto request) {
        try {
            String body = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(datalabUrl))
                    .timeout(Duration.ofSeconds(30))   // 읽기 타임아웃: 30초
                    .header("Content-Type", "application/json")
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                // 어떤 요청이 실패했는지 로그로 확인 가능
                log.error("[NaverDatalab] HTTP {} — body: {}", response.statusCode(), response.body());
                throw new RuntimeException("네이버 API 오류: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), NaverDatalabResponseDto.class);

        } catch (Exception e) {
            log.error("[NaverDatalab] 호출 실패 — {}", e.getMessage());
            throw new RuntimeException("데이터랩 API 호출 실패: " + e.getMessage(), e);
        }
    }
}