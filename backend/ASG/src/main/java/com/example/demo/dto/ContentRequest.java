package com.example.demo.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContentRequest {
    private String menuName;
    private String extraInfo;
    private List<String> keywords;
    private String platforms;
    private String tones;
    private String emojiLevel;
    private int maxLength;
    private String imageUrl;
    private Long userId; // 세션에서 주입, null이면 BRAND_ID=1L fallback
}