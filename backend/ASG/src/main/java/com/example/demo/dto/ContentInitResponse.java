package com.example.demo.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentInitResponse {
    private List<KeywordDto> keywords;
    private List<SnsGuideDto> snsGuides;
    private UserSettingDto userSetting;
    
    @Getter
    @Builder
    public static class KeywordDto {
        private String keywordName;
        private String category;
    }

    @Getter
    @Builder
    public static class SnsGuideDto {
        private String platform;
        private String guideContent;
        private String bestTime;
    }

    @Getter
    @Builder
    public static class UserSettingDto {
        private List<String> activeSns;
        private String toneStyle;
        private String emojiLevel;
        private int maxLength;
    }
}