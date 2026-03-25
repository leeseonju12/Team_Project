package com.example.demo.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class PlatformKeywordResponseDto {
    private String brand;
    private List<String> instagram;
    private List<String> youtube;
    private List<String> naver;
    private List<String> google;
}