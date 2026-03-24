package com.example.demo.service;

import com.example.demo.entity.SocialMetric;
import com.example.demo.repository.SocialMetricRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialAnalyticsService {

    private final SocialMetricRepository metricRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${facebook.api.page-access-token}")
    private String pageAccessToken;

    // 🌟 yml에 있는 페이지 ID를 바로 끌어옵니다!
    @Value("${facebook.api.page-id}")
    private String pageId;

    public void syncAllFacebookPosts(Long userId) {
        try {
            // 게시물 기본 정보 및 반응 데이터 일괄 조회 (조회수 제외)
            String url = String.format(
                "https://graph.facebook.com/v18.0/%s/published_posts?fields=id,likes.summary(true).limit(0),comments.summary(true).limit(0),shares&access_token=%s",
                pageId, pageAccessToken
            );

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode dataArray = root.path("data");

            int savedCount = 0;
            int updatedCount = 0;

            for (JsonNode postNode : dataArray) {
                String postId = postNode.path("id").asText();

                // DB 조회 후 존재하지 않으면 새 엔티티 객체 생성
                SocialMetric metric = metricRepository.findByUserIdAndPlatformAndTargetId(userId, "FACEBOOK", postId)
                        .orElseGet(() -> new SocialMetric());

                // 신규 엔티티인 경우 기본 식별값 세팅
                if (metric.getId() == null) {
                    metric.setUserId(userId);
                    metric.setPlatform("FACEBOOK");
                    metric.setTargetType("POST");
                    metric.setTargetId(postId);
                    savedCount++;
                } else {
                    updatedCount++;
                }

                // 최신 지표 데이터 갱신
                metric.setRecordDate(LocalDate.now());
                metric.setLikesCount(postNode.path("likes").path("summary").path("total_count").asInt(0));
                metric.setCommentsCount(postNode.path("comments").path("summary").path("total_count").asInt(0));
                metric.setSharesCount(postNode.path("shares").path("count").asInt(0));

                // 개별 게시물 조회수 데이터 분리 호출
                Integer viewsCount = null;
                try {
                    String insightUrl = String.format(
                        "https://graph.facebook.com/v18.0/%s/insights?metric=post_impressions&access_token=%s",
                        postId, pageAccessToken
                    );
                    ResponseEntity<String> insightResponse = restTemplate.getForEntity(insightUrl, String.class);
                    JsonNode insightData = mapper.readTree(insightResponse.getBody()).path("data");
                    
                    if (insightData.isArray() && !insightData.isEmpty()) {
                        viewsCount = insightData.get(0).path("values").get(0).path("value").asInt();
                    }
                } catch (Exception e) {
                    // API 에러 또는 지표 미제공 시 null 값 유지
                }
                
                metric.setViewsCount(viewsCount);

                // 변경된 상태를 DB에 반영
                metricRepository.save(metric);
            }

            System.out.println("페이스북 동기화 완료 (신규: " + savedCount + "건, 갱신: " + updatedCount + "건)");

        } catch (Exception e) {
            System.err.println("페이스북 전체 지표 수집 실패: " + e.getMessage());
        }
    }
    
 // 🌟 페이스북 페이지의 게시물 목록(ID, 내용, 작성일)만 가볍게 가져오기
    public List<Map<String, String>> getFacebookPostList() {
        List<Map<String, String>> postList = new ArrayList<>();
        
        try {
            // 게시물 ID, 글 내용, 작성 시간만 딱 가져오는 가벼운 API 호출
            String url = String.format(
                "https://graph.facebook.com/v18.0/%s/published_posts?fields=id,message,created_time&access_token=%s",
                pageId, pageAccessToken
            );

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode dataArray = mapper.readTree(response.getBody()).path("data");

            for (JsonNode postNode : dataArray) {
                Map<String, String> postData = new HashMap<>();
                postData.put("postId", postNode.path("id").asText());
                
                // 사진만 올리고 글을 안 썼을 수도 있으니 null 체크
                String message = postNode.has("message") ? postNode.path("message").asText() : "(내용 없음)";
                postData.put("message", message);
                
                postData.put("createdTime", postNode.path("created_time").asText());
                
                postList.add(postData);
            }
            return postList;

        } catch (Exception e) {
            System.err.println("게시물 목록 불러오기 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}