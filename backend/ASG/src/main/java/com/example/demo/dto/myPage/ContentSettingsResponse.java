package com.example.demo.dto.myPage;

import com.example.demo.domain.ContentSettings;
import lombok.Getter;

@Getter
public class ContentSettingsResponse {

    private final Long settingsId;
    private final String tone;
    private final String emojiLevel;
    private final Integer targetLength;
    private final String introTemplate;
    private final String outroTemplate;
    private final String preferredSns;
    private final Boolean useDefaultMode;  // ✅ 추가

    public ContentSettingsResponse(ContentSettings settings) {
        this.settingsId    = settings.getId();
        this.tone          = settings.getTone();
        this.emojiLevel    = settings.getEmojiLevel();
        this.targetLength  = settings.getTargetLength();
        this.introTemplate = settings.getIntroTemplate();
        this.outroTemplate = settings.getOutroTemplate();
        this.preferredSns  = settings.getPreferredSns();  // ← 추가
        this.useDefaultMode  = settings.getUseDefaultMode();  // ✅ 추가
    }
}
