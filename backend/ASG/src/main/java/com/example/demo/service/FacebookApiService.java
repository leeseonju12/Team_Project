package com.example.demo.service;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.domain.CustomerFeedback;
import com.example.demo.domain.enums.AiStatus;
import com.example.demo.domain.enums.FeedbackStatus;
import com.example.demo.domain.enums.FeedbackType;
import com.example.demo.domain.enums.Platform;
import com.example.demo.entity.FacebookComment;
import com.example.demo.repository.FacebookCommentRepository;
import com.example.demo.repository.FeedbackRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class FacebookApiService {


	// !!! 개인 계정 말고 페이지만 가능! 페이지 토큰 !!!
    @Value("${facebook.api.page-access-token}")
    private String pageAccessToken;

    @Value("${facebook.api.page-id}")
    private String pageId;
    
    @Autowired
    private FacebookCommentRepository commentRepository;
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final String GRAPH_API_BASE_URL = "https://graph.facebook.com/v19.0";

    public String publishPost(String imageUrl, String message) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // 🌟 페이스북은 /photos 엔드포인트로 보내면 사진+글 한 방에 업로드 끝!
            String url = "https://graph.facebook.com/v18.0/" + pageId + "/photos";

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("url", imageUrl);
            params.add("message", message);
            params.add("access_token", pageAccessToken);

            ResponseEntity<String> response = restTemplate.postForEntity(url, params, String.class);
            JsonNode rootNode = mapper.readTree(response.getBody());

            // 성공하면 생성된 페이스북 포스트 ID를 돌려줍니다.
            return rootNode.get("post_id").asText();

        } catch (Exception e) {
            throw new RuntimeException("페이스북 API 에러: " + e.getMessage());
        }
    }
    
    @Transactional
    public int fetchAndSaveAllPageComments() {
        RestTemplate restTemplate = new RestTemplate();
        int savedCount = 0;
        
        System.out.println("=== 페이스북 동기화 시작 - 페이지 ID: " + pageId + " ===");
        

//        String url = String.format("%s/%s/posts?fields=id,comments{id,message,created_time,from}&access_token=%s", 
//                                   GRAPH_API_BASE_URL, pageId, pageAccessToken);

//        URI uri = UriComponentsBuilder.fromUriString(GRAPH_API_BASE_URL + "/" + pageId + "/posts")
//                .queryParam("fields", "id,comments{id,message,created_time,from}")
//                .queryParam("access_token", pageAccessToken)
//                .build()
//                .encode()
//                .toUri();
        
        String rawUrl = GRAPH_API_BASE_URL + "/" + pageId + "/feed?fields=id,comments%7Bid,message,created_time,from%7D&access_token=" + pageAccessToken;
        URI uri = URI.create(rawUrl);
        
        try {
        	ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        	
        	System.out.println(">>> 페이스북 응답 원본: " + response.getBody());
            
            // 🌟 2. 이미 선언해둔 objectMapper를 써서 우리가 직접 JsonNode로 쪼갭니다!
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode postsData = rootNode.path("data");

            if (postsData != null && postsData.isArray()) {
                for (JsonNode post : postsData) {
                    String postId = post.path("id").asText();
                    JsonNode commentsNode = post.path("comments").path("data");

                    if (!commentsNode.isMissingNode() && commentsNode.isArray()) {
                    	for (JsonNode comment : commentsNode) {
                    	    String commentId = comment.path("id").asText();
                    	    String message = comment.path("message").asText();
                    	    String authorName = comment.path("from").isMissingNode() ? "Unknown" : comment.path("from").path("name").asText();
                    	    
                    	    // 🌟 1. 페이스북이 준 실제 작성 시간(created_time) 꺼내기
                    	    String createdTimeStr = comment.path("created_time").asText();
                    	    LocalDateTime realCreatedAt = LocalDateTime.now(); // 기본값
                    	    
                    	    try {
                    	        if (createdTimeStr != null && !createdTimeStr.isEmpty()) {
                    	        	DateTimeFormatter fbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
                    	        	realCreatedAt = OffsetDateTime.parse(createdTimeStr, fbFormatter)
                    	        	                              .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                    	        	                              .toLocalDateTime();
                    	        }
                    	    } catch (Exception e) {
                    	        System.err.println("시간 파싱 에러 (기본값 사용): " + e.getMessage());
                    	    }

                    	    String postUrl = "https://www.facebook.com/" + postId; 

                    	    Optional<CustomerFeedback> existing = feedbackRepository.findByExternalId(commentId);

                    	    if (existing.isPresent()) {
                    	        CustomerFeedback feedback = existing.get();
                    	        feedback.setOriginalText(message);
                    	        feedback.setAuthorName(authorName);
                    	        // 이미 있는 댓글은 시간 업데이트를 안 하는 게 일반적입니다.
                    	        feedbackRepository.save(feedback);
                    	    } else {
                    	        CustomerFeedback newFeedback = CustomerFeedback.builder()
                    	                .externalId(commentId)
                    	                .authorName(authorName)
                    	                .originalText(message)
                    	                .originUrl(postUrl)
                    	                .platform(Platform.FACEBOOK)
                    	                .type(FeedbackType.COMMENT) 
                    	                .status(FeedbackStatus.UNRESOLVED) 
                    	                .aiStatus(AiStatus.IDLE)
                    	                .createdAt(realCreatedAt) // 🌟 3. .now() 대신 진짜 작성 시간으로 교체!
                    	                .build();
                    	        
                    	        feedbackRepository.save(newFeedback);
                    	        savedCount++; 
                    	    }
                    	}
                    }
                }
            }
            return savedCount;
            
        } catch (RestClientResponseException e) {
            System.err.println("=== 페이스북 댓글 동기화 API 실패 ===");
            System.err.println("에러 메시지: " + e.getResponseBodyAsString());
            throw new RuntimeException("페이스북 동기화 실패 (API 연동 에러)");
        } catch (Exception e) {
            System.err.println("페이스북 댓글 동기화 내부 에러: " + e.getMessage());
            throw new RuntimeException("페이스북 동기화 에러", e);
        }
    }
    
    // (선택 사항) 대시보드에서 작성한 답글을 페이스북 서버로 쏘는 로직
    public void replyToComment(String commentId, String message) {
        RestTemplate restTemplate = new RestTemplate();
        // FB는 특정 댓글에 대댓글을 달 때 /{comment_id}/comments 를 씁니다.
        String url = GRAPH_API_BASE_URL + "/" + commentId + "/comments";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("message", message);
        params.add("access_token", pageAccessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            System.out.println("=== 페이스북 답글 전송 시도 - 대상 ID: " + commentId + " ===");
            restTemplate.postForEntity(url, request, String.class);
            System.out.println(">>> 페이스북 답글 전송 성공!");
        } catch (RestClientResponseException e) {
            // 페이스북이 뱉어낸 진짜 에러 메시지 확인
            System.err.println("페이스북 답글 API 에러: " + e.getResponseBodyAsString());
            throw new RuntimeException("페이스북 답글 전송 실패: 권한이나 ID를 확인하세요.");
        } catch (Exception e) {
            System.err.println("페이스북 답글 내부 에러: " + e.getMessage());
            throw new RuntimeException("페이스북 답글 전송 에러");
        }
    }
    
    
}