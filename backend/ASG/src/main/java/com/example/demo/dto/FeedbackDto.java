package com.example.demo.dto;

import lombok.Builder;
import com.example.demo.domain.CustomerFeedback;
import com.example.demo.domain.enums.Platform;

import lombok.Data;

@Data
@Builder
public class FeedbackDto {
    private Long id;
    private String name;			//it.name 매핑 (authorName)
    private String type;
    private String platform;
    private String platformLabel;
    private String platClass;
    private String avatar;
    private String avatarColor;
    private String text;             // 원문
    private String status;
    private String aiReply;
    private String aiStatus;
    private String sentReply;        // 최종 전송 답글
    private String originUrl;
    private String createdAt;
    
    
    public static FeedbackDto fromEntity(CustomerFeedback entity) {
        return FeedbackDto.builder()
                .id(entity.getId())
                .name(entity.getAuthorName())
                .text(entity.getOriginalText())
                .platformLabel(getKoreanLabel(entity.getPlatform()))
                .platClass(getCssClass(entity.getPlatform()))
                .avatar(entity.getAuthorName() != null && !entity.getAuthorName().isEmpty() 
                        ? entity.getAuthorName().substring(0, 1) : "?")
                .originUrl(entity.getOriginUrl())
                .type(entity.getType() != null ? entity.getType().name().toLowerCase() : "")
                .status(entity.getStatus() != null ? entity.getStatus().name().toLowerCase() : "")
                .aiStatus(entity.getAiStatus() != null ? entity.getAiStatus().name().toLowerCase() : "")
                .aiReply(entity.getAiReply())
                .sentReply(entity.getSentReply())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : "")
                .build();
    }
    
    private static String getKoreanLabel(Platform platform) {
        if (platform == null) return "";
        return switch (platform.name()) {
            case "INSTAGRAM" -> "인스타그램";
            case "FACEBOOK" -> "페이스북";
            case "NAVER" -> "네이버";
            case "KAKAO" -> "카카오";
            case "GOOGLE" -> "구글";
            default -> platform.name();
        };
    }

    private static String getCssClass(Platform platform) {
        if (platform == null) return "";
        return switch (platform.name()) {
            case "INSTAGRAM" -> "plat-ig";
            case "FACEBOOK" -> "plat-fb";
            case "NAVER" -> "plat-nv";
            case "KAKAO" -> "plat-kk";
            case "GOOGLE" -> "plat-gm";
            default -> "plat-" + platform.name().toLowerCase();
        };
    }
}