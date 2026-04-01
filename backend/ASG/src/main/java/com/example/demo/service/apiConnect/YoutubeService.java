package com.example.demo.service.apiConnect;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 검색 결과
유튜브 검색어 추출
유튜브 api 사용중

*/

@Service
@RequiredArgsConstructor
public class YoutubeService {

    @Value("${youtube.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON에서 다음 페이지 토큰을 찾기 위함

    /**
     * 유튜브 데이터를 100개(50개씩 2페이지) 수집합니다.
     */
    public List<String> getYoutubeData(BrandSearchRequestDto request) {
        List<String> responses = new ArrayList<>();
        String nextPageToken = null; // 다음 페이지로 가는 '열쇠'

        // 💡 50개씩 2번 반복하여 100개 수집
        for (int i = 0; i < 2; i++) {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("key", apiKey)
                    .queryParam("part", "snippet") // 제목, 설명 등을 가져옴
                    .queryParam("q", request.getBrandName())
                    .queryParam("type", "video")   // 동영상만 검색
                    .queryParam("publishedAfter", getPublishedAfterDate(request.getPeriod())) // 시작 날짜 필터
                    .queryParam("maxResults", 50); // 유튜브 API 한 번 호출 최댓값

            // 💡 두 번째 페이지 호출일 경우 토큰을 주소에 추가
            if (nextPageToken != null) {
                builder.queryParam("pageToken", nextPageToken);
            }

            // 최종 API 주소 생성
            String apiURL = builder.build().toUriString();

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(apiURL, String.class);
                String body = response.getBody();
                responses.add(body);
                System.out.println("====> [유튜브] 페이지 " + (i+1) + " 수집 완료");

                // 💡 응답 JSON에서 "nextPageToken"을 찾아서 다음 루프에서 사용
                JsonNode root = objectMapper.readTree(body);
                if (root.has("nextPageToken")) {
                    nextPageToken = root.get("nextPageToken").asText();
                } else {
                    break; // 다음 페이지가 없으면 루프 종료
                }
            } catch (Exception e) {
            	 System.err.println("====> [유튜브] 페이지 " + (i+1) + " 호출 실패: " + e.getMessage());
            	    responses.add("{\"error\":\"유튜브 페이지 " + (i+1) + " 호출 실패: " + e.getMessage() + "\"}");
            	    break;
            }
        }
        return responses;
    }

    /**
     * 주/월/년 토글에 맞춰 RFC 3339 날짜 형식 생성
     */
    private String getPublishedAfterDate(String period) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime targetDate = switch (period != null ? period : "month") {
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "year" -> now.minusYears(1);
            default -> now.minusMonths(1);
        };
        // 유튜브 API 필수 형식 (예: 2024-05-20T00:00:00Z)
        return targetDate.format(DateTimeFormatter.ISO_INSTANT);
    }
}