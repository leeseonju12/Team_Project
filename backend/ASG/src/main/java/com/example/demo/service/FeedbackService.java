package com.example.demo.service; // 본인 패키지명으로 변경하세요

import com.example.demo.domain.CustomerFeedback;
import com.example.demo.domain.FeedbackSource;
import com.example.demo.dto.FeedbackResponseDto;
import com.example.demo.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    
    private final OpenAiService openAiService; // 추가

    public List<FeedbackResponseDto> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Entity -> DTO 변환 로직
    private FeedbackResponseDto convertToDto(CustomerFeedback feedback) {
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

        return FeedbackResponseDto.builder()
                .id(feedback.getId())
                .name(authorName)
                .type(feedback.getType() != null ? feedback.getType().name().toLowerCase() : "")
                .platform(platformString)
                .platformLabel(platformLabel)
                .platClass(platClass)
                .avatar(avatarChar)
                .avatarColor(avatarColor)
                .text(originalText)
                .status(feedback.getStatus() != null ? feedback.getStatus().name().toLowerCase() : "")
                .aiReply(feedback.getAiReply() != null ? feedback.getAiReply() : "")
                .aiStatus(feedback.getAiStatus() != null ? feedback.getAiStatus().name().toLowerCase() : "idle")
                .sentReply(feedback.getSentReply() != null ? feedback.getSentReply() : "")
                .build();
    }
    
 // 💡 단건 AI 답변 생성 로직
    @Transactional // DB 값을 변경(Update)하므로 꼭 필요합니다!
    public FeedbackResponseDto generateAiReply(Long feedbackId) {
        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰/댓글이 존재하지 않습니다. ID: " + feedbackId));

        // TODO 교체
        String authorName = feedback.getSource().getAuthorName();
        String reviewText = feedback.getSource().getOriginalText();
        String generatedReply = openAiService.generateReply(authorName, reviewText);

        // 엔티티 업데이트 (JPA 더티 체킹으로 인해 save()를 안 해도 DB에 자동 반영됨)
        feedback.updateAiReply(generatedReply);

        // 변경된 최신 상태를 다시 DTO로 변환해서 프론트로 반환
        return convertToDto(feedback);
    }

    /* 임시 AI 답변 생성기 (프론트엔드에 있던 로직을 백엔드로 가져옴)
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
    }*/
}