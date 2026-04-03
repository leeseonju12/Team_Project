package com.example.demo.service; // 본인 패키지명으로 변경하세요

import com.example.demo.domain.CustomerFeedback;
import com.example.demo.domain.enums.AiStatus;
import com.example.demo.domain.enums.FeedbackStatus;
import com.example.demo.domain.enums.FeedbackType;
import com.example.demo.domain.enums.Platform;
import com.example.demo.dto.FeedbackDto;
import com.example.demo.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
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
    private final FacebookApiService facebookApiService;
    
    /*
    @Value("${ai.api-key}")
    private String geminiApiKey;*/

//    public List<FeedbackDto> getAllFeedbacks() {
//        return feedbackRepository.findAll().stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//    }
    
	  public List<CustomerFeedback> getAllFeedbacks() {
		  return feedbackRepository.findAllByOrderByIdAsc();
	}

    // Entity -> DTO 변환 로직
    private FeedbackDto convertToDto(CustomerFeedback feedback) {
        String platformLabel = "";
        String platClass = "";
        String avatarColor = "";
        
        String platformString = ""; // 프론트엔드 필터링용 문자열
        
        String authorName = (feedback.getAuthorName() != null && !feedback.getAuthorName().isEmpty()) 
                ? feedback.getAuthorName() : "?";
        String originalText = feedback.getOriginalText() != null ? feedback.getOriginalText() : "";
        String avatarChar = "?";
        
        if (feedback.getPlatform() != null) {
            // Platform Enum을 기준으로 UI 스타일 결정
            switch (feedback.getPlatform()) {
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
                .originUrl(feedback.getOriginUrl())
                .build();
    }
    
 // 💡 단건 AI 답변 생성 로직
    @Transactional // DB 값을 변경(Update)하므로 꼭 필요합니다!
    public FeedbackDto generateAiReply(Long feedbackId) {
        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰/댓글이 존재하지 않습니다. ID: " + feedbackId));

        String author = feedback.getAuthorName();
        String text = feedback.getOriginalText();
        
        String prompt = String.format(
        	    "Role: Cafe Manager. Task: Reply to customer review. Lang: Korean.\n" +
        	    "Tone: Warm, friendly (like a local regular).\n" +
        	    "Customer: %s\n" +
        	    "Message: %s\n" +
        	    "MaxLen: 50 chars.\n" +
        	    "Rule: Address their point sensibly. Return ONLY the reply text without quotes.",
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

 // 💡 전송 완료 처리 로직
    @Transactional
    public FeedbackDto sendReply(Long feedbackId) {
        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰/댓글이 존재하지 않습니다. ID: " + feedbackId));

        String replyText = feedback.getAiReply();
        
        if (feedback.getPlatform() == Platform.INSTAGRAM) {
        	
            String commentId = feedback.getExternalId();
            instagramApiService.replyToComment(commentId, replyText);
        }    
        else if (feedback.getPlatform() == Platform.FACEBOOK) {
            
            System.out.println(">>> 페이스북 답글 전송 시작: " + feedback.getExternalId());
            facebookApiService.replyToComment(feedback.getExternalId(), replyText);
        }
        
        feedback.sendReply(); // 엔티티 비즈니스 로직 호출 (상태 변경 및 sentReply 세팅)

        return convertToDto(feedback);
    }
    
    @Transactional(readOnly = true)
    public Page<CustomerFeedback> getFeedbacks(String sourceGroup, String sourceValue, String statusValue, String keyword, Pageable pageable) {
        Specification<CustomerFeedback> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 리뷰/댓글 출처 필터
            if ("review_group".equals(sourceGroup)) {
                predicates.add(cb.equal(root.get("type"), FeedbackType.REVIEW));
                if (sourceValue != null && !sourceValue.isEmpty() && !sourceValue.equals("review_all")) {
                    String plat = sourceValue.split("_")[0].toUpperCase();
                    predicates.add(cb.equal(root.get("platform"), Platform.valueOf(plat)));
                }
            } else if ("comment_group".equals(sourceGroup)) {
                predicates.add(cb.equal(root.get("type"), FeedbackType.COMMENT));
                if (sourceValue != null && !sourceValue.isEmpty() && !sourceValue.equals("comment_all")) {
                    String plat = sourceValue.split("_")[0].toUpperCase();
                    predicates.add(cb.equal(root.get("platform"), Platform.valueOf(plat)));
                }
            }

            // 2. 처리 상태 필터
            if ("answer_done".equals(statusValue)) {
                predicates.add(cb.or(
                    cb.equal(root.get("aiStatus"), AiStatus.DONE),
                    cb.equal(root.get("status"), FeedbackStatus.COMPLETED)
                ));
            } else if ("answer_needed".equals(statusValue)) {
                predicates.add(cb.and(
                    cb.notEqual(root.get("aiStatus"), AiStatus.DONE),
                    cb.notEqual(root.get("status"), FeedbackStatus.COMPLETED)
                ));
            }

            // 3. 검색어 필터
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("authorName")), pattern),
                    cb.like(cb.lower(root.get("originalText")), pattern),
                    cb.like(cb.lower(root.get("aiReply")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return feedbackRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getKpiSummary() {
        long total = feedbackRepository.count();
        long completed = feedbackRepository.countByStatus(FeedbackStatus.COMPLETED);
        long ready = feedbackRepository.countByAiStatusAndStatusNot(AiStatus.DONE, FeedbackStatus.COMPLETED);
        long need = total - completed - ready;
        
        return Map.of("total", total, "need", need, "ready", ready, "completed", completed);
    }

}