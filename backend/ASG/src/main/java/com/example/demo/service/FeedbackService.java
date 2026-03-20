package com.example.demo.service; // 본인 패키지명으로 변경하세요

import com.example.demo.domain.CustomerFeedback;
import com.example.demo.domain.FeedbackSource;
import com.example.demo.domain.enums.Platform;
import com.example.demo.dto.FeedbackDto;
import com.example.demo.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final GeminiApiClient geminiApiClient;
    private final InstagramApiService instagramApiService;
    
    /*
    @Value("${ai.api-key}")
    private String geminiApiKey;*/

    public List<FeedbackDto> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Entity -> DTO 변환 로직
    private FeedbackDto convertToDto(CustomerFeedback feedback) {
        String platformLabel = "";
        String platClass = "";
        String avatarColor = "";
        
        String platformString = ""; // 프론트엔드 필터링용 문자열
        
        String authorName = "?";
        String originalText = "";
        String avatarChar = "?";
        
        FeedbackSource source = feedback.getSource();
        if (source != null) {
            authorName = source.getAuthorName() != null ? source.getAuthorName() : "?";
            originalText = source.getOriginalText() != null ? source.getOriginalText() : "";
            avatarChar = authorName.length() > 0 ? authorName.substring(0, 1) : "?";

            if (source.getPlatform() != null) {
                // DB의 깔끔한 Enum(NAVER 등)을 프론트엔드가 쓰던 클래스와 포맷으로 변환
                switch (source.getPlatform()) {
                    case NAVER -> {
                        platformLabel = "네이버 지도"; platClass = "plat-nv"; avatarColor = "#03C75A";
                        platformString = "naver_review";
                    }
                    case KAKAO -> {
                        platformLabel = "카카오맵"; platClass = "plat-kk"; avatarColor = "#F59E0B";
                        platformString = "kakao_review";
                    }
                    case GOOGLE -> {
                        platformLabel = "구글 리뷰"; platClass = "plat-gm"; avatarColor = "#EA4335";
                        platformString = "google_review";
                    }
                    case INSTAGRAM -> {
                        platformLabel = "인스타그램"; platClass = "plat-ig"; avatarColor = "#E1306C";
                        platformString = "instagram_comment";
                    }
                    case FACEBOOK -> {
                        platformLabel = "페이스북"; platClass = "plat-fb"; avatarColor = "#1877F2";
                        platformString = "facebook_comment";
                    }
                }
            }
        }
        
        
        String status = "unresolved";
        if (feedback.getSentReply() != null && !feedback.getSentReply().isBlank()) {
            status = "completed";
        } 
        // 답글 전송은 안 했지만 AI가 답변을 만들어뒀다면? -> 답변 생성됨
        else if (feedback.getAiReply() != null && !feedback.getAiReply().isBlank()) {
            status = "generated";
        }
        // status 관련 이후에 꼭 옮기자...

        return FeedbackDto.builder()
                .id(feedback.getId())
                .name(authorName)
                .type(feedback.getType() != null ? feedback.getType().name().toLowerCase() : "")
                .platform(platformString)
                .platformLabel(platformLabel)
                .platClass(platClass)
                .avatar(avatarChar)
                .avatarColor(avatarColor)
                .text(originalText)
                //.status(feedback.getStatus() != null ? feedback.getStatus().name().toLowerCase() : "")
                .status(status)
                .aiReply(feedback.getAiReply() != null ? feedback.getAiReply() : "")
                .aiStatus(feedback.getAiStatus() != null ? feedback.getAiStatus().name().toLowerCase() : "idle")
                .sentReply(feedback.getSentReply() != null ? feedback.getSentReply() : "")
                .originUrl(feedback.getSource().getOriginUrl())
                .build();
    }
    
 // 💡 단건 AI 답변 생성 로직
    @Transactional // DB 값을 변경(Update)하므로 꼭 필요합니다!
    public FeedbackDto generateAiReply(Long feedbackId) {
        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰/댓글이 존재하지 않습니다. ID: " + feedbackId));

        String author = feedback.getSource().getAuthorName();
        String text = feedback.getSource().getOriginalText();
        
        String prompt = String.format(
                "당신은 요식업 매장을 운영하는 사장님입니다.\n" +
                "다음 고객의 리뷰나 댓글을 읽고, 동네 단골을 대하듯 따뜻한 말투로 답글을 작성바람.\n" +
                "고객이 언급한 것에 대해 센스 있게 답변해주세요\n\n" +
                "고객 이름:%s\n고객 메시지:%s\n글자수 제한: 50자 내외", 
                author != null ? author : "고객", 
                text != null ? text : ""
        );
        
        String generatedReply = geminiApiClient.requestToGemini(prompt);
        if ("[]".equals(generatedReply) || generatedReply == null || generatedReply.isBlank()) {
            generatedReply = "AI 답변을 생성하는 중 서버와 통신 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
        }
        // 엔티티 업데이트 (JPA 더티 체킹으로 인해 save()를 안 해도 DB에 자동 반영됨)
        feedback.updateAiReply(generatedReply);

        // 변경된 최신 상태를 다시 DTO로 변환해서 프론트로 반환
        return convertToDto(feedback);
    }
    
    /*
    private String callRealLLM(CustomerFeedback feedback) {
        String author = feedback.getSource().getAuthorName();
        String text = feedback.getSource().getOriginalText();

        // 1. 프롬프트(지시문) 엔지니어링 
        String prompt = String.format(
        		"당신은 배달도 하는 동네 인기 카페의 다정하고 친절한 사장님" +
        	    "다음 고객의 리뷰나 댓글을 읽고, 동네 단골을 대하듯 따뜻한 말투로 답글을 작성바람" +
        	    "고객이 언급한 것에 대해 센스 있게 답변해주세요\n\n" +
        	    "고객 이름:%s\n고객 메시지:%s\n글자수 제한:100자내외", author, text
        );
        
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + geminiApiKey;
        
        // 3. HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // JSON 바디 생성
        // 토큰 로직 추가(generationConfig)
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            ),
            "generationConfig", Map.of(
                    //"maxOutputTokens", 800,
                    "temperature", 1.5      // 0.0(딱딱하고 기계적) ~ 2.0(창의적이고 감성적) 사이의 톤 조절
            )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            // 5. API 쏘고 응답받기!
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            // 6. 복잡한 JSON 응답 구조에서 텍스트 알맹이만 파싱해서 꺼내오기
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            
            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            e.printStackTrace();
            return "AI 답변을 생성하는 중 서버와 통신 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
        }
    }
    */


    /*
    // 임시 AI 답변 생성기 (프론트엔드에 있던 로직을 백엔드로 가져옴)
    private String mockAiGeneration(CustomerFeedback feedback) {
        String author = feedback.getSource().getAuthorName();
        String text = feedback.getSource().getOriginalText().toLowerCase();
        
        if (text.contains("언제") || text.contains("예약") || text.contains("문의")) {
            //return author + "님, 문의 주셔서 감사합니다. 요청하신 내용은 확인 후 빠르게 안내해 드리겠습니다!";
        	return author + "님, 문의 받았다요 문의 답변이다요";
        } else if (text.contains("아쉽") || text.contains("불편") || text.contains("조금")) {
            //return author + "님, 이용에 불편을 드려 죄송합니다. 말씀해주신 부분은 내부적으로 꼭 점검하여 개선하겠습니다.";
            return author + "님 불편사항 암튼 대충 AI 답변";
        } else {
            return author + "님, 소중한 리뷰 고맙다요";
        }
    }
    */
    
    
 // 💡 전송 완료 처리 로직
    @Transactional
    public FeedbackDto sendReply(Long feedbackId) {
        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰/댓글이 존재하지 않습니다. ID: " + feedbackId));

        // 인스타
        if (feedback.getSource().getPlatform() == Platform.INSTAGRAM) {
            String commentId = feedback.getSource().getExternalId(); // 저장해둔 인스타 댓글 ID
            String replyText = feedback.getAiReply(); // AI가 만든 답변

            // 2. 🌟 진짜 인스타그램 서버에 답글 전송!
            instagramApiService.replyToComment(commentId, replyText);
        }
        
        feedback.sendReply(); // 엔티티 비즈니스 로직 호출 (상태 변경 및 sentReply 세팅)

        return convertToDto(feedback);
    }
}