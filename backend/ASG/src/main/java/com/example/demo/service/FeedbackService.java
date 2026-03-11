package com.example.demo.service; // 본인 패키지명으로 변경하세요

import com.example.demo.domain.CustomerFeedback;
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
        
        // 프론트엔드 CSS 클래스 및 라벨에 맞게 매핑
        if (feedback.getPlatform() != null) {
            switch (feedback.getPlatform()) {
                case NAVER_REVIEW -> { platformLabel = "네이버 지도"; platClass = "plat-nv"; avatarColor = "#03C75A"; }
                case KAKAO_REVIEW -> { platformLabel = "카카오맵"; platClass = "plat-kk"; avatarColor = "#F59E0B"; }
                case GOOGLE_REVIEW -> { platformLabel = "구글 리뷰"; platClass = "plat-gm"; avatarColor = "#EA4335"; }
                case INSTAGRAM_COMMENT -> { platformLabel = "인스타그램"; platClass = "plat-ig"; avatarColor = "#E1306C"; }
                case FACEBOOK_COMMENT -> { platformLabel = "페이스북"; platClass = "plat-fb"; avatarColor = "#1877F2"; }
            }
        }

        // 이름의 첫 글자를 아바타에 사용 (예방 차원의 null 체크 포함)
        String avatarChar = (feedback.getAuthorName() != null && !feedback.getAuthorName().isEmpty()) 
                            ? feedback.getAuthorName().substring(0, 1) : "?";

        return FeedbackResponseDto.builder()
                .id(feedback.getId())
                .name(feedback.getAuthorName())
                .type(feedback.getType() != null ? feedback.getType().name().toLowerCase() : "")
                .platform(feedback.getPlatform() != null ? feedback.getPlatform().name().toLowerCase() : "")
                .platformLabel(platformLabel)
                .platClass(platClass)
                .avatar(avatarChar)
                .avatarColor(avatarColor)
                .text(feedback.getOriginalText())
                .status(feedback.getStatus() != null ? feedback.getStatus().name().toLowerCase() : "")
                .aiReply(feedback.getAiReply() != null ? feedback.getAiReply() : "")
                .aiStatus(feedback.getAiStatus() != null ? feedback.getAiStatus().name().toLowerCase() : "idle")
                .sentReply(feedback.getSentReply() != null ? feedback.getSentReply() : "")
                .build();
    }
}