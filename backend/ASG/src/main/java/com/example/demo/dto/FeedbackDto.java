package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackDto {
    private Long id;
    private String name;
    private String type;             // "review" or "comment"
    private String platform;         // "naver_review", "instagram_comment" 등
    private String platformLabel;    // "네이버 지도" (프론트엔드 렌더링용)
    private String platClass;        // "plat-nv" (CSS 클래스)
    private String avatar;           // "김" (이름 첫 글자)
    private String avatarColor;      // "#03C75A" 
    private String text;             // 원문
    private String status;           // "unresolved", "completed" 등
    private String aiReply;          // AI 답변
    private String aiStatus;         // "idle" or "done"
    private String sentReply;        // 최종 전송 답글
    private String originUrl;
}