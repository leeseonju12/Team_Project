package com.example.demo.service;

import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = HttpClient.newBuilder()
            	    .connectTimeout(Duration.ofSeconds(60))
            	    .build()
            	    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("네이버 API 오류: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), NaverDatalabResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("데이터랩 API 호출 실패: " + e.getMessage(), e);
        }
    }
}