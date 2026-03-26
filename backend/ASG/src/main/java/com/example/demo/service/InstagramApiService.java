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

import java.util.List;

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

@Service
@RequiredArgsConstructor
public class InstagramApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String GRAPH_API_BASE_URL = "https://graph.facebook.com/v19.0";
    
    private final InstagramCommentRepository commentRepository;
    private final FeedbackRepository feedbackRepository; // 통합 레포
    
    @Value("${instagram.api.access-token}")
    private String accessToken;
    
    @Value("${instagram.api.account-ids}")
    private String accountId;

 // 🌟 yml에 적어둔 여러 개의 계정 ID를 한 번에 불러옵니다.
    @Value("${instagram.api.account-ids}")
    private List<String> systemAccountIds;

    @Transactional
    public int syncAllInstagramComments() {
        int newCommentCount = 0;
        try {
            // 🌟 등록된 2~3개의 계정을 하나씩 돌면서 피드를 긁어옵니다!
            for (String igAccountId : systemAccountIds) {
                System.out.println("=== 동기화 시작 - 인스타 계정 ID: " + igAccountId + " ===");
                
                // 해당 계정의 전체 피드(게시물 목록) 가져오기
                JsonNode feed = getInstagramFeed(igAccountId);
                
                
                if (feed != null && feed.isArray()) {
                    for (JsonNode media : feed) {
                        String mediaId = media.get("id").asText();
                        String permalink = media.path("permalink").asText("");
                        // 각 게시물의 댓글을 수집하고 DB에 저장
                        newCommentCount += fetchAndSaveCommentsForMedia(mediaId, permalink);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("전체 댓글 동기화 중 에러: " + e.getMessage());
        }
        return newCommentCount;
    }
    
    // 1. 인스타그램 비즈니스 계정 ID 조회
    public String getInstagramAccountId() {
    	System.out.println("=== getInstagramAccountId 메서드 진입! ===");
        System.out.println("현재 세팅된 토큰: " + accessToken);
        String url = GRAPH_API_BASE_URL + "/me/accounts?fields=instagram_business_account&access_token=" + accessToken;
        try {

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            System.out.println("=== 페이스북 정상 응답 ===");
            System.out.println(response.getBody());
            
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode data = rootNode.get("data");

            if (data != null && data.isArray() && data.size() > 0) {
                for (JsonNode page : data) {
                    if (page.has("instagram_business_account")) {
                        return page.get("instagram_business_account").get("id").asText();
                    }
                }
            }
        } catch (RestClientResponseException e) {
            // 🌟 페이스북이 400 에러 등을 던지면 여기서 잡힙니다!
            System.err.println("=== 페이스북 API 통신 실패 (에러 원본) ===");
            System.err.println("상태 코드: " + e.getStatusCode());
            System.err.println("에러 메시지: " + e.getResponseBodyAsString());
            System.err.println("=========================================");
            throw new RuntimeException("연결된 인스타그램 비즈니스 계정을 찾을 수 없습니다. RestClientResponseException e");
            
        } catch (Exception e) {
            System.err.println("인스타그램 계정 ID 조회 실패: " + e.getMessage());
            System.err.println("yml 첫번째 아이디 연동합니다." + systemAccountIds.get(0));
            if (systemAccountIds != null && !systemAccountIds.isEmpty())
                return systemAccountIds.get(0);
        }
        throw new RuntimeException("연결된 인스타그램 비즈니스 계정을 찾을 수 없습니다. Exception e");
    } 
    

    // 2. 인스타그램 피드 조회
    public JsonNode getInstagramFeed(String igAccountId) {
        String fields = "id,caption,media_type,media_url,permalink,timestamp";
        //String url = GRAPH_API_BASE_URL + "/" + igAccountId + "/media?fields=" + fields + "&access_token=" + accessToken;
        String url = GRAPH_API_BASE_URL + "/" + igAccountId + "/media?fields=id,caption,timestamp,permalink&access_token=" + accessToken;
        
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
    public String publishInstagramPost(String igAccountId, String imageUrl, String caption) {
        String containerId = createMediaContainer(igAccountId, imageUrl, caption);
        return publishMedia(igAccountId, containerId);
    }

    // Step 1: 미디어 컨테이너 생성 (Body 전송 방식 - 한글 깨짐 방지)
    private String createMediaContainer(String igAccountId, String imageUrl, String caption) {
        String url = GRAPH_API_BASE_URL + "/" + igAccountId + "/media";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //String testUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/1200px-Image_created_with_a_mobile_phone.png";
        String testUrl = "https://upload.wikimedia.org/wikipedia/commons/a/a3/June_odd-eyed-cat.jpg";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        //map.add("image_url", imageUrl);
        map.add("image_url", testUrl);
        map.add("caption", caption);
        map.add("access_token", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            System.out.println(">>> 인스타그램 JSON 방식 테스트 전송 시작");
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            System.out.println(">>> 컨테이너 생성 성공! ID: " + rootNode.get("id").asText());
            return rootNode.get("id").asText();
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorDetail = e.getResponseBodyAsString();
            System.err.println("인스타그램 Step 1 에러 상세: " + errorDetail);
            throw new RuntimeException("인스타그램 API 에러: " + errorDetail);
        } catch (Exception e) {
            throw new RuntimeException("게시물 임시 저장 실패: " + e.getMessage());
        }
    }

    // Step 2: 게시물 최종 발행
    private String publishMedia(String igAccountId, String creationId) {
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
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 인스타그램 API가 리턴한 상세 에러 바디 확인
            String errorDetail = e.getResponseBodyAsString();
            System.err.println("게시물 최종 발행 실패 상세: " + errorDetail);
            throw new RuntimeException("인스타그램 발행 실패: " + errorDetail);
            
        } catch (Exception e) {
            throw new RuntimeException("게시물 최종 발행 실패: " + e.getMessage());
        }
    }

    // DB에 저장해서 그걸 불러오는 방식
    @Transactional
    public JsonNode getCommentsAndSave(String mediaId) {
        String fields = "id,text,timestamp,username,like_count,media{permalink}";
        String url = GRAPH_API_BASE_URL + "/" + mediaId + "/comments?fields=" + fields + "&access_token=" + accessToken;
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode dataArray = rootNode.get("data"); // 댓글 배열
        

            if (dataArray != null && dataArray.isArray()) {
                // 배열을 돌면서 하나씩 DB에 저장
                for (JsonNode node : dataArray) {
                	
                	String originUrl = node.path("media").path("permalink").asText("");
                	
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
    public void replyToComment(String commentId, String message) {
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
    
    /**
     * 특정 게시물의 댓글을 가져와 중복 검사 후 저장하는 내부 메서드
     */
    private int fetchAndSaveCommentsForMedia(String mediaId, String permalink) {
        int count = 0;
        String fields = "id,text,timestamp,username,like_count";
        String url = GRAPH_API_BASE_URL + "/" + mediaId + "/comments?fields=" + fields + "&access_token=" + accessToken;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode dataArray = objectMapper.readTree(response.getBody()).get("data");

            if (dataArray != null && dataArray.isArray()) {
                for (JsonNode node : dataArray) {
                    String commentId = node.get("id").asText(); // 인스타그램 고유 댓글 ID

                    // 🌟 핵심: DB에 이미 이 댓글 ID가 있는지 검사! 없으면 저장합니다.
                    if (!feedbackRepository.existsBySource_ExternalId(commentId)) {
                        
                        FeedbackSource source = FeedbackSource.builder()
                                .externalId(commentId) // 여기에 고유 ID 저장
                                .authorName(node.path("username").asText("unknown"))
                                .originalText(node.path("text").asText(""))
                                .platform(com.example.demo.domain.enums.Platform.INSTAGRAM)
                                .originUrl(permalink)
                                .build();

                        CustomerFeedback feedback = CustomerFeedback.builder()
                                .source(source)
                                .type(FeedbackType.COMMENT)
                                .aiStatus(AiStatus.IDLE)
                                .build();
                        
                        feedbackRepository.save(feedback);
                        count++; // 새로 저장된 개수 증가
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("게시물(" + mediaId + ") 댓글 수집 에러: " + e.getMessage());
        }
        return count;
    }
    
    /*
    public String publishPost(String imageUrl, String caption) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // ==========================================
            // 1단계: 미디어 컨테이너 생성 (임시 업로드)
            // ==========================================
            String containerUrl = "https://graph.facebook.com/v18.0/" + accountId + "/media";
            
            MultiValueMap<String, String> containerParams = new LinkedMultiValueMap<>();
            containerParams.add("image_url", imageUrl);
            containerParams.add("caption", caption);
            containerParams.add("access_token", accessToken);

            // 무식하지만 가장 안전하게 String으로 받아서 까기!
            ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, containerParams, String.class);
            JsonNode containerNode = mapper.readTree(containerResponse.getBody());
            String containerId = containerNode.get("id").asText();

            // ==========================================
            // 2단계: 실제 피드에 게시 (Publish)
            // ==========================================
            String publishUrl = "https://graph.facebook.com/v18.0/" + accountId + "/media_publish";
            
            MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
            publishParams.add("creation_id", containerId);
            publishParams.add("access_token", accessToken);

            ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishParams, String.class);
            JsonNode publishNode = mapper.readTree(publishResponse.getBody());
            
            // 최종적으로 생성된 인스타그램 게시물 ID 반환
            return publishNode.get("id").asText();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 인스타그램 API가 응답한 실제 JSON 에러 바디 추출
            String errorBody = e.getResponseBodyAsString();
            
            // 콘솔에 상세 에러 출력
            System.err.println("인스타그램 API 상세 에러: " + errorBody);
            
            // 프론트엔드로 상세 에러 전달
            throw new RuntimeException("인스타그램 API 에러 상세: " + errorBody);
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("서버 내부 시스템 에러: " + e.getMessage());
        }
    }
    */
    
    public String publishPost(String imageUrl, String caption) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // 🌟 1. 앞뒤 혹시 모를 공백이나 엔터값 완벽 제거
            String cleanImageUrl = imageUrl.trim();
            System.out.println(">>> [최종 확인] 인스타그램으로 보낼 이미지 URL: " + cleanImageUrl);

            // ==========================================
            // 1단계: 미디어 컨테이너 생성 (가장 안정적인 Form 전송 방식)
            // ==========================================
            String containerUrl = "https://graph.facebook.com/v18.0/" + accountId + "/media";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> containerParams = new LinkedMultiValueMap<>();
            // 🔥 절대 수동 인코딩(URLEncoder)을 하지 마세요! 스프링이 알아서 합니다.
            containerParams.add("image_url", cleanImageUrl); 
            containerParams.add("caption", caption);
            containerParams.add("access_token", accessToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(containerParams, headers);

            ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, request, String.class);
            JsonNode containerNode = mapper.readTree(containerResponse.getBody());
            String containerId = containerNode.get("id").asText();

            System.out.println(">>> 1단계 성공! 컨테이너 ID: " + containerId + " (3초 대기 중...)");
            Thread.sleep(3000); // 🌟 다운로드 시간 필수 대기

            // ==========================================
            // 2단계: 실제 피드에 게시 (Publish)
            // ==========================================
            String publishUrl = "https://graph.facebook.com/v18.0/" + accountId + "/media_publish";
            
            MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
            publishParams.add("creation_id", containerId);
            publishParams.add("access_token", accessToken);

            HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
            
            ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);
            JsonNode publishNode = mapper.readTree(publishResponse.getBody());
            
            String finalMediaId = publishNode.get("id").asText();
            System.out.println(">>> 2단계 성공! 인스타그램 최종 게시물 ID: " + finalMediaId);
            
            return finalMediaId;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            System.err.println("인스타그램 API 상세 에러: " + errorBody);
            throw new RuntimeException("인스타그램 API 에러 상세: " + errorBody);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("서버 내부 시스템 에러: " + e.getMessage());
        }
    }
    
}