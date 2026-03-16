package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class InstagramApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String GRAPH_API_BASE_URL = "https://graph.facebook.com/v19.0";

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
}