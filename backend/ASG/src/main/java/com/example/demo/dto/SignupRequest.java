package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SignupRequest {

    // ── Screen 2: 기본 정보 ──────────────────────────────────
    private String nickname;
    private String contactPhone;
    private String storePhoneNumber;   // 가게 전화번호 추가
    private String companyName;
    private String businessCategory;
    private String preferredChannel;
    private String locationName;   // 지점명 추가

    // ── Screen 3-①: 주소 
    private String roadAddrPart1;   // 도로명주소 본체
    private String addrDetail;      // 사용자 직접 입력 상세주소

    // ── Screen 3-②: 영업시간 (0=월 ~ 6=일, 7개 고정) ─────────
    @NotNull(message = "영업시간 정보가 누락되었습니다.")
    @Size(min = 7, max = 7, message = "영업시간은 7일치 모두 전송해야 합니다.")
    @Valid
    private List<BusinessHourItem> businessHours;

    @Getter
    @Setter
    public static class BusinessHourItem {

        @Min(0) @Max(6)
        private int dayOfWeek;

        private boolean open;

        // open=false 면 빈 문자열 허용, open=true 면 HH:mm 필수
        @Pattern(
            regexp = "^$|^([01]?[0-9]|2[0-3]):[0-5][0-9]$",
            message = "영업 시작 시간 형식이 올바르지 않습니다. (HH:mm)"
        )
        private String openTime;

        @Pattern(
            regexp = "^$|^([01]?[0-9]|2[0-3]):[0-5][0-9]$",
            message = "영업 종료 시간 형식이 올바르지 않습니다. (HH:mm)"
        )
        private String closeTime;
    }

    // ── 약관 ─────────────────────────────────────────────────
 // boolean 대신 String으로 받아 바인딩 오류 원천 차단
    private String termsAgreed;
    private String privacyAgreed;
    private String locationAgreed;
    private String marketingConsent;
    private String eventConsent;

    // 서비스에서 사용할 때 호출할 헬퍼 메서드
    public boolean isTermsAgreed() { return "true".equalsIgnoreCase(termsAgreed) || "1".equals(termsAgreed); }
    public boolean isPrivacyAgreed() { return "true".equalsIgnoreCase(privacyAgreed) || "1".equals(privacyAgreed); }
    public boolean isLocationAgreed() { return "true".equalsIgnoreCase(locationAgreed) || "1".equals(locationAgreed); }

    // ── Screen 4: AI 콘텐츠 설정 (전체 선택) ─────────────────
    @Size(max = 100, message = "인트로 템플릿은 100자 이내로 입력해주세요.")
    private String introTemplate;

    @Size(max = 100, message = "아웃트로 템플릿은 100자 이내로 입력해주세요.")
    private String outroTemplate;

    private String tone;       // 기본 | 친근 | 깔끔 | 격식 | 트렌디
    private String emojiLevel; // 적게 | 적당히 | 많이

    @Min(value = 100, message = "글자수는 100자 이상이어야 합니다.")
    @Max(value = 500, message = "글자수는 500자 이하이어야 합니다.")
    private Integer targetLength;
}