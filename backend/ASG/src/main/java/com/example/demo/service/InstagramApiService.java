package com.example.demo.service;

import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.InstagramCommentRepository;
import com.example.demo.domain.CustomerFeedback;
import com.example.demo.domain.FeedbackSource;
import com.example.demo.domain.enums.AiStatus;
import com.example.demo.domain.enums.FeedbackType;
import com.example.demo.entity.InstagramComment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class InstagramApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String GRAPH_API_BASE_URL = "https://graph.facebook.com/v19.0";
    
    private final InstagramCommentRepository commentRepository;
    private final FeedbackRepository feedbackRepository; // 통합 레포

    // 1. 인스타그램 비즈니스 계정 ID 조회
    public String getInstagramAccountId(String accessToken) {
        String url = GRAPH_API_BASE_URL + "/me/accounts?fields=instagram_business_account&access_token=" + accessToken;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode data = rootNode.get("data");

            if (data != null && data.isArray() && data.size() > 0) {
                for (JsonNode page : data) {
                    if (page.has("instagram_business_account")) {
                        return page.get("instagram_business_account").get("id").asText();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("인스타그램 계정 ID 조회 실패: " + e.getMessage());
        }
        throw new RuntimeException("연결된 인스타그램 비즈니스 계정을 찾을 수 없습니다.");
    }

    // 2. 인스타그램 피드 조회
    public JsonNode getInstagramFeed(String igAccountId, String accessToken) {
        String fields = "id,caption,media_type,media_url,permalink,timestamp";
        String url = GRAPH_API_BASE_URL + "/" + igAccountId + "/media?fields=" + fields + "&access_token=" + accessToken;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.get("data");
        } catch (Exception e) {
            System.err.println("피드 조회 에러: " + e.getMessage());
            return null;
        }
    }

    // 3. 인스타그램 게시물 통합 발행 (2-Step)
    public String publishInstagramPost(String igAccountId, String imageUrl, String caption, String accessToken) {
        String containerId = createMediaContainer(igAccountId, imageUrl, caption, accessToken);
        return publishMedia(igAccountId, containerId, accessToken);
    }

    // Step 1: 미디어 컨테이너 생성 (Body 전송 방식 - 한글 깨짐 방지)
    private String createMediaContainer(String igAccountId, String imageUrl, String caption, String accessToken) {
        String url = GRAPH_API_BASE_URL + "/" + igAccountId + "/media";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("image_url", imageUrl);
        map.add("caption", caption);
        map.add("access_token", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("게시물 임시 저장 실패: " + e.getMessage());
        }
    }

    // Step 2: 게시물 최종 발행
    private String publishMedia(String igAccountId, String creationId, String accessToken) {
        String url = GRAPH_API_BASE_URL + "/" + igAccountId + "/media_publish";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("creation_id", creationId);
        map.add("access_token", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("게시물 최종 발행 실패: " + e.getMessage());
        }
    }
    
    
     // 특정 게시물의 댓글 목록을 가져옵니다.

    public JsonNode getComments(String mediaId, String accessToken) {
        // 가져올 필드: 댓글 ID, 내용, 작성시간, 작성자 정보
        String fields = "id,text,timestamp,username,like_count";
        String url = GRAPH_API_BASE_URL + "/" + mediaId + "/comments?fields=" + fields + "&access_token=" + accessToken;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            // 댓글 목록은 "data" 배열 안에 들어있습니다.
            return rootNode.get("data");
        } catch (Exception e) {
            System.err.println("댓글 조회 중 에러 발생: " + e.getMessage());
            return null;
        }
    }

    // DB에 저장해서 그걸 불러오는 방식
    @Transactional
    public JsonNode getCommentsAndSave(String mediaId, String accessToken) {
        String fields = "id,text,timestamp,username,like_count";
        String url = GRAPH_API_BASE_URL + "/" + mediaId + "/comments?fields=" + fields + "&access_token=" + accessToken;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode dataArray = rootNode.get("data"); // 댓글 배열

            if (dataArray != null && dataArray.isArray()) {
                // 배열을 돌면서 하나씩 DB에 저장
                for (JsonNode node : dataArray) {
                	
                	// 🌟 A. 통합 DB용 '출처' 정보 생성
                    FeedbackSource source = FeedbackSource.builder()
                    		.externalId(node.get("id").asText())
                            .authorName(node.path("username").asText("unknown"))
                            .originalText(node.path("text").asText(""))
                            .platform(com.example.demo.domain.enums.Platform.INSTAGRAM)
                            .build();

                    // 🌟 B. 통합 DB용 '피드백' 객체 생성
                    CustomerFeedback feedback = CustomerFeedback.builder()
                            .source(source)
                            .type(FeedbackType.COMMENT)
                            .aiStatus(AiStatus.IDLE)
                            .build();
                	
                    // 🌟 C. 통합 레포지토리에 저장! (이래야 대시보드에 나옵니다)
                    feedbackRepository.save(feedback);
                	
                	/*
                    InstagramComment comment = new InstagramComment();
                    comment.setId(node.get("id").asText());
                    comment.setMediaId(mediaId); // 🌟 파라미터로 받은 미디어 ID 저장
                    comment.setUsername(node.path("username").asText("unknown")); // 혹시 없을 경우 대비
                    comment.setText(node.path("text").asText(""));
                    comment.setTimestamp(node.path("timestamp").asText(""));
                    comment.setLikeCount(node.path("like_count").asInt(0));
                    

                	
                    // DB에 저장! (이미 같은 ID가 있으면 UPDATE, 없으면 INSERT 해줍니다)
                    commentRepository.save(comment);
                    */
	
                }
            }
            return dataArray;

        } catch (Exception e) {
            System.err.println("댓글 조회/저장 중 에러 발생: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 인스타그램 서버에 실제 답글을 전송하는 핵심 메서드
     */
    public void replyToComment(String commentId, String message, String accessToken) {
        // /{comment-id}/replies 엔드포인트 사용
        String url = GRAPH_API_BASE_URL + "/" + commentId + "/replies";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("message", message);
        map.add("access_token", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            // 인스타그램은 성공 시 생성된 답글의 ID를 반환합니다.
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("인스타그램 답글 전송 실패: " + e.getMessage());
        }
    }
}