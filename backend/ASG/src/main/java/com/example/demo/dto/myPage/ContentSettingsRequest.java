package com.example.demo.dto.myPage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentSettingsRequest {

    private String tone;          // 기본 / 친근 / 깔끔 / 격식 / 트렌디
    private String emojiLevel;    // 적게 / 적당히 / 많이
    private Integer targetLength; // 100 ~ 500
    private String introTemplate;
    private String outroTemplate;
}
