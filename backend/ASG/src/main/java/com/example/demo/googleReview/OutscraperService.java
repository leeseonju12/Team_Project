package com.example.demo.googleReview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutscraperService {

    // v3로 변경
    private static final String API_URL  = "https://api.app.outscraper.com/maps/reviews-v3";
    private static final String PLATFORM = "GOOGLE";

    @Value("${outscraper.api-key}")
    private String apiKey;

    private final GoogleReviewRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void scrapeAndSave(String query, int reviewsLimit) throws Exception {

        String url = UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("query", query)
                .queryParam("reviewsLimit", reviewsLimit)
                .queryParam("async", false)
                .queryParam("language", "ko")
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
        }

        List<GoogleReviewEntity> list = parseReviews(response.getBody());
        repository.saveAll(list);
        System.out.println("저장 완료: " + list.size() + "건");
    }

    private List<GoogleReviewEntity> parseReviews(String body) throws Exception {
        List<GoogleReviewEntity> result = new ArrayList<>();
        JsonNode root = objectMapper.readTree(body);

        JsonNode dataArray = root.path("data");

        if (dataArray.isMissingNode() || dataArray.isEmpty()) {
            System.out.println("⚠️ data 배열이 비어있습니다. 응답 전체: " + body);
            return result;
        }

        for (JsonNode place : dataArray) {
            JsonNode reviews = place.path("reviews_data");

            for (JsonNode review : reviews) {
                // ✅ 올바른 필드명: author_title
                String author = review.path("author_title").asText("").trim();
                String text   = review.path("review_text").asText("").trim();
                int    rating = review.path("review_rating").asInt(0);

                // 빈 값 스킵
                if (author.isEmpty() || text.isEmpty()) {
                    System.out.printf("SKIP - author='%s', text='%s'%n", author, text);
                    continue;
                }

                // author 최대 100자 (DB 컬럼 제한)
                if (author.length() > 100) author = author.substring(0, 100);

                result.add(new GoogleReviewEntity(author, PLATFORM, text));
            }
        }

        return result;
    }
}