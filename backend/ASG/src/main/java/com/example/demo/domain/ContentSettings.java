package com.example.demo.domain;

import com.example.demo.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
 
/**
 * AI 콘텐츠 생성 개인 설정 엔티티
 * User와 1:1 관계
 */
@Entity
@Table(name = "content_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ContentSettings {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    /**
     * 연관된 사용자 (1:1, FK)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
 
    /**
     * 인트로 템플릿 (선택, 최대 100자)
     * 예: "안녕하세요! ☀️ {가게명}입니다."
     */
    @Column(name = "intro_template", length = 100)
    private String introTemplate;
 
    /**
     * 아웃트로 템플릿 (선택, 최대 100자)
     * 예: "많은 관심과 방문 부탁드립니다! 💚"
     */
    @Column(name = "outro_template", length = 100)
    private String outroTemplate;
 
    /**
     * 말투 (기본 | 친근 | 깔끔 | 격식 | 트렌디)
     * null → "기본" 으로 간주
     */
    @Column(name = "tone", length = 20)
    @Builder.Default
    private String tone = "기본";
 
    /**
     * 이모지 양 (적게 | 적당히 | 많이)
     * null → "적당히" 으로 간주
     */
    @Column(name = "emoji_level", length = 10)
    @Builder.Default
    private String emojiLevel = "적당히";
 
    /**
     * 글자수 목표 (100~500, step 50)
     * null → 300 으로 간주
     */
    @Column(name = "target_length")
    @Builder.Default
    private Integer targetLength = 300;
    
    // SNS 기본 선택값
    @Column(name = "preferred_sns", length = 100)
    private String preferredSns;  // "instagram,naver" 형태로 저장
 
    // 기본 세팅 저장
    @Column(name = "use_default_mode", nullable = false)
    @Builder.Default
    private Boolean useDefaultMode = true;
    
    
    // ── 정적 팩토리 ──────────────────────────────────────────
 
    /**
     * 기본값으로 생성 (온보딩 screen4 스킵 시 호출)
     */
    public static ContentSettings createDefault(User user) {
        return ContentSettings.builder()
                .user(user)
                .tone("기본")
                .emojiLevel("적당히")
                .targetLength(300)
                .useDefaultMode(true)  // ✅ 추가
                .build();
    }
 
    /**
     * 온보딩 입력값으로 생성
     * 입력 없이 대시보드로 이동했을 시 기본상태
     */
    public static ContentSettings createFromOnboarding(
            User user,
            String introTemplate,
            String outroTemplate,
            String tone,
            String emojiLevel,
            Integer targetLength
    ) {
        return ContentSettings.builder()
                .user(user)
                .introTemplate(introTemplate)
                .outroTemplate(outroTemplate)
                .tone(tone != null ? tone : "기본")
                .emojiLevel(emojiLevel != null ? emojiLevel : "적당히")
                .targetLength(targetLength != null ? targetLength : 300)
                .useDefaultMode(false)   // ← 이 한 줄 추가
                .build();
    }
 
    /**
     * 설정 업데이트 (마이페이지 등에서 재사용)
     */
    public void update(
            String introTemplate,
            String outroTemplate,
            String tone,
            String emojiLevel,
            Integer targetLength,
            String preferredSns,
            Boolean useDefaultMode
    ) {
        if (introTemplate != null) this.introTemplate = introTemplate;
        if (outroTemplate != null) this.outroTemplate = outroTemplate;
        if (tone != null) this.tone = tone;
        if (emojiLevel != null) this.emojiLevel = emojiLevel;
        if (targetLength != null) this.targetLength = targetLength;
        if (preferredSns != null) this.preferredSns = preferredSns;
        if (useDefaultMode != null) this.useDefaultMode = useDefaultMode;
    }
}