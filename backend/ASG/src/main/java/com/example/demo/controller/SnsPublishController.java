package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class SnsPublishController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GRAPH_API_BASE = "https://graph.facebook.com/v19.0/";

    // 프론트에서 보낼 DTO 클래스 내부 선언 (또는 별도 파일로 분리)
    public static class PublishRequestDto {
        private String caption;
        private String imageUrl; // 인스타그램은 로컬 이미지가 아닌 퍼블릭 URL이 필수입니다.

        // Getter, Setter
        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    @PostMapping("/api/sns/publish/instagram")
    public ResponseEntity<?> publishToInstagram(
            @RequestBody PublishRequestDto request,
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RegisteredOAuth2AuthorizedClient("facebook") OAuth2AuthorizedClient authorizedClient) {

        try {
            // 1. 발급받은 액세스 토큰 가져오기
            String accessToken = authorizedClient.getAccessToken().getTokenValue();

            
            // 💡 주의: 인스타그램 API에 글을 올리려면 'Instagram Business Account ID'를 먼저 알아야 합니다.
            // 원래는 Me -> Pages -> Instagram Business Account 순서로 API를 호출해 ID를 가져와야 합니다.
            // (이 부분은 테스트를 위해 하드코딩하거나 추가 로직이 필요합니다.)
            String instagramAccountId = "17841446406635677"; 

            // =========================================================
            // [1단계] 컨테이너 생성 (Create Container)
            // =========================================================
            // 이미지 URL과 캡션을 보내어 업로드 준비 상태로 만듭니다.
            String createContainerUrl = GRAPH_API_BASE + instagramAccountId + "/media"
                    + "https://th.bing.com/th/id/OIP.A-9pW8S6X1_7pS9U-Vj0OAHaHa?rs=1&pid=ImgDetMain" // 임시 퍼블릭 이미지
                    + "&caption=" + request.getCaption()
                    + "&access_token=" + accessToken;

            Map<String, Object> containerResponse = restTemplate.postForObject(createContainerUrl, null, Map.class);
            String creationId = (String) containerResponse.get("id");

            if (creationId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "컨테이너 생성 실패"));
            }

            // =========================================================
            // [2단계] 생성된 컨테이너 게시 (Publish)
            // =========================================================
            String publishUrl = GRAPH_API_BASE + instagramAccountId + "/media_publish"
                    + "?creation_id=" + creationId
                    + "&access_token=" + accessToken;

            Map<String, Object> publishResponse = restTemplate.postForObject(publishUrl, null, Map.class);
            String finalPostId = (String) publishResponse.get("id");

            return ResponseEntity.ok(Map.of(
                    "message", "성공",
                    "postId", finalPostId
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "API 호출 중 에러 발생: " + e.getMessage()));
        }
    }
}