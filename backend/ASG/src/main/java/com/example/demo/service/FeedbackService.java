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
}