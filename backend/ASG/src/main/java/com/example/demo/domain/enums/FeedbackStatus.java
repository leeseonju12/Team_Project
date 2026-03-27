package com.example.demo.domain.enums;

public enum FeedbackStatus {
    UNRESOLVED,  // 미해결 (확인 필요)
    CHECKED,     // 확인됨
    UNCHECKED,   // 미확인
    SENDING,     // 전송 중
    COMPLETED    // 전송 완료 (최종)
}