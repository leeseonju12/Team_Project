package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class ContentRequest {
    private String menuName;
    private String extraInfo;
    private List<String> keywords;
    private String platforms;
    private String tones;
    private String emojiLevel;
    private int maxLength;
}