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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    
    private static final DateTimeFormatter FACEBOOK_DATE_FORMAT = 
    	    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    @Value("${facebook.api.page-access-token}")
    private String pageAccessToken;

    // 🌟 yml에 있는 페이지 ID를 바로 끌어옵니다!
    @Value("${facebook.api.page-id}")
    private String pageId;

    public void syncAllFacebookPosts(Long userId) {
        try {
            // 게시물 기본 정보 및 반응 데이터 일괄 조회 (조회수 등 제외)
            String url = String.format(
            		"https://graph.facebook.com/v18.0/%s/published_posts?fields=id,created_time,likes.summary(true).limit(0),comments.summary(true).limit(0),shares&access_token=%s",
                    pageId, pageAccessToken
            );

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode dataArray = mapper.readTree(response.getBody()).path("data");

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
                    
                    String createdTime = postNode.path("created_time").asText();
                    metric.setPostPublishedDate(ZonedDateTime.parse(createdTime, FACEBOOK_DATE_FORMAT).toLocalDate());
                } else {
                    updatedCount++;
                }

                // 최신 지표 데이터 갱신
                metric.setLastSyncedAt(LocalDateTime.now());
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

                metricRepository.save(metric);
            }

            System.out.println("페이스북 동기화 완료 (신규: " + savedCount + "건, 갱신: " + updatedCount + "건)");

        } catch (Exception e) {
            System.err.println("페이스북 전체 지표 수집 실패: " + e.getMessage());
        }
    }
    
   
    //========================
    
    @Value("${instagram.api.account-ids}")
    private String instagramAccountId;

    @Value("${instagram.api.access-token}")
    private String instagramAccessToken;

    public void syncAllInstagramPosts(Long userId) {
        try {
            // 인스타그램 미디어 목록 및 기본 지표(좋아요, 댓글) 일괄 조회
            String url = String.format(
            	"https://graph.facebook.com/v18.0/%s/media?fields=id,media_type,timestamp,like_count,comments_count&access_token=%s",
                instagramAccountId, instagramAccessToken
            );

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode dataArray = mapper.readTree(response.getBody()).path("data");

            int savedCount = 0;
            int updatedCount = 0;

            for (JsonNode mediaNode : dataArray) {
                String mediaId = mediaNode.path("id").asText();
                String mediaType = mediaNode.path("media_type").asText();

                // DB 조회 후 존재하지 않으면 새 엔티티 객체 생성
                SocialMetric metric = metricRepository.findByUserIdAndPlatformAndTargetId(userId, "INSTAGRAM", mediaId)
                        .orElseGet(() -> new SocialMetric());

                // 신규 엔티티 식별값 세팅
                if (metric.getId() == null) {
                    metric.setUserId(userId);
                    metric.setPlatform("INSTAGRAM");
                    metric.setTargetType(mediaType); // IMAGE, VIDEO, CAROUSEL_ALBUM 등
                    metric.setTargetId(mediaId);
                    savedCount++;
                    
                    String timestamp = mediaNode.path("timestamp").asText();
                    metric.setPostPublishedDate(ZonedDateTime.parse(timestamp, FACEBOOK_DATE_FORMAT).toLocalDate());
                } else {
                    updatedCount++;
                }

                // 최신 기본 지표 데이터 갱신
                metric.setLastSyncedAt(LocalDateTime.now());
                metric.setLikesCount(mediaNode.path("like_count").asInt(0));
                metric.setCommentsCount(mediaNode.path("comments_count").asInt(0));

                // 인스타그램 인사이트 지표 (조회수/도달수, 저장수) 분리 호출
                Integer viewsCount = null;
                Integer sharesCount = null; // 인스타그램 API는 저장수(saved)를 제공하므로 이를 활용

                try {
                    // 미디어 타입에 따라 지원하는 인사이트 지표가 다름 (릴스 vs 일반 게시물)
                    String insightMetric = mediaType.equals("VIDEO") ? "plays,saved" : "impressions,saved";
                    String insightUrl = String.format(
                        "https://graph.facebook.com/v18.0/%s/insights?metric=%s&access_token=%s",
                        mediaId, insightMetric, instagramAccessToken
                    );
                    
                    ResponseEntity<String> insightResponse = restTemplate.getForEntity(insightUrl, String.class);
                    JsonNode insightData = mapper.readTree(insightResponse.getBody()).path("data");

                    if (insightData.isArray() && !insightData.isEmpty()) {
                        for (JsonNode insight : insightData) {
                            String name = insight.path("name").asText();
                            int value = insight.path("values").get(0).path("value").asInt(0);
                            
                            if (name.equals("impressions") || name.equals("plays")) {
                                viewsCount = value;
                            } else if (name.equals("saved")) {
                                sharesCount = value; 
                            }
                        }
                    }
                } catch (Exception e) {
                    // API 에러 또는 지표 미제공 시 null 값 유지
                }

                metric.setViewsCount(viewsCount);
                metric.setSharesCount(sharesCount); // 저장수를 공유수 컬럼에 매핑하거나, 필요시 savedCount 컬럼 추가 권장

                metricRepository.save(metric);
            }

            System.out.println("인스타그램 동기화 완료 (신규: " + savedCount + "건, 갱신: " + updatedCount + "건)");

        } catch (Exception e) {
            System.err.println("인스타그램 전체 지표 수집 실패: " + e.getMessage());
        }
    }
}