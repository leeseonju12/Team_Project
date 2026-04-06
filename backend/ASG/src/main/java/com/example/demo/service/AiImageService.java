package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import com.example.demo.dto.AiImageRequestDto;
import com.example.demo.dto.AiImageResponseDto;
import com.example.demo.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AiImageService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private static final String GENERATE_ENDPOINT = "/api/ai/generate";

    // ✅ AppConfig에서 설정된 Builder 주입
    public AiImageService(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public AiImageResponseDto generateImage(String koreanKeyword, String category) {
        validateRequiredInputs(koreanKeyword, category);
        log.info("AI 이미지 생성 요청 시작 - 키워드: {}, 업종: {}", koreanKeyword, category);

        AiImageRequestDto requestDto = new AiImageRequestDto(koreanKeyword, category);

        try {
            return restClient.post()
                    .uri(GENERATE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestDto)
                    .retrieve()
                    .onStatus(status -> status.isError(), (request, response) -> {
                        try {
                            JsonNode errorJson = objectMapper.readTree(response.getBody());
                            String step = errorJson.has("step") ? errorJson.get("step").asText() : "UNKNOWN";
                            String message = errorJson.has("error") ? errorJson.get("error").asText() : "AI 서버 오류";
                            log.error("Flask 에러 발생 [단계: {}] : {}", step, message);
                            throw new AiServiceException(message, step);
                        } catch (Exception e) {
                            log.error("에러 응답 파싱 실패: {}", e.getMessage());
                            throw new AiServiceException("AI 서버 처리 중 오류가 발생했습니다.", "SERVER_ERROR");
                        }
                    })
                    .body(AiImageResponseDto.class);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 서버 통신 중 예외 발생: {}", e.getMessage());
            throw new AiServiceException("AI 서버와 통신할 수 없습니다.", "COMMUNICATION_ERROR", e);
        }
    }

    private void validateRequiredInputs(String keyword, String category) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수 입력 사항입니다.");
        }
        if (category == null || category.isBlank()) {
            log.warn("이미지 생성 중단: 사용자 업종 정보가 없습니다.");
            throw new AiServiceException("사용자 업종 정보가 설정되지 않아 이미지를 생성할 수 없습니다.", "MISSING_CATEGORY");
        }
    }
}