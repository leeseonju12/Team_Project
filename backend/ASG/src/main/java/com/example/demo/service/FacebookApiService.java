package com.example.demo.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FacebookApiService {


	// !!! 개인 계정 말고 페이지만 가능! 페이지 토큰 !!!
    @Value("${facebook.api.page-access-token}")
    private String pageAccessToken;

    @Value("${facebook.api.page-id}")
    private String pageId;

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
}