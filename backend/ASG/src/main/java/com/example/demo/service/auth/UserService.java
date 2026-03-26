package com.example.demo.service.auth;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.demo.domain.ContentSettings;
import com.example.demo.domain.user.entity.BusinessHours;
import com.example.demo.domain.user.entity.User;
import com.example.demo.dto.SignupRequest;
import com.example.demo.repository.auth.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));
    }

    @Transactional(readOnly = true)
    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Transactional
    public void completeSignup(Long userId, SignupRequest dto) {

        // ── 1. 기존 영속 User 조회 ───────────────────────────
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

        // ── 2. 기본 정보 + 동의 ───────────────────────────
        user.completeSignup(
                dto.getNickname(),
                dto.getContactPhone(),
                dto.getCompanyName(),
                dto.getBusinessCategory(),
                dto.getPreferredChannel(),
                dto.getStorePhoneNumber(),
                
                dto.isTermsAgreed(), 
                dto.isPrivacyAgreed(), 
                dto.isLocationAgreed(),
                
                "true".equalsIgnoreCase(dto.getMarketingConsent()) || "1".equals(dto.getMarketingConsent()),
                "true".equalsIgnoreCase(dto.getEventConsent()) || "1".equals(dto.getEventConsent())
            );

        // ── 3. 주소 저장 (수정: 엔티티의 updateAddress(zipNo, roadAddr, detail) 순서에 맞춤) ──
        user.updateAddress(
            dto.getRoadAddrPart1(),   // 2. 도로명주소
            dto.getAddrDetail()       // 3. 상세주소
        );

        // ── 4. 영업시간 저장 ─────────────────────────────────
        user.replaceBusinessHours(buildBusinessHours(user, dto));

        // ── 5. AI 콘텐츠 설정 저장 ───────────────────────────
        user.applyContentSettings(buildContentSettings(user, dto));

        log.info("회원가입 온보딩 완료 - userId={}, nickname={}", userId, user.getNickname());
    }

    // ── private helpers (변경 없음) ──────────────────────────────

    private List<BusinessHours> buildBusinessHours(User user, SignupRequest dto) {
        if (dto.getBusinessHours() == null) {
            log.warn("영업시간 데이터가 전달되지 않았습니다. userId={}", user.getId());
            return List.of();
        }

        return dto.getBusinessHours().stream()
            .map(item -> item.isOpen()
                ? BusinessHours.openDay(user, item.getDayOfWeek(), item.getOpenTime(), item.getCloseTime())
                : BusinessHours.closedDay(user, item.getDayOfWeek()))
            .collect(Collectors.toList());
    }

    private ContentSettings buildContentSettings(User user, SignupRequest dto) {
        boolean hasInput = StringUtils.hasText(dto.getIntroTemplate())
            || StringUtils.hasText(dto.getOutroTemplate())
            || StringUtils.hasText(dto.getTone())
            || StringUtils.hasText(dto.getEmojiLevel())
            || dto.getTargetLength() != null;

        if (!hasInput) {
            return ContentSettings.createDefault(user);
        }

        return ContentSettings.createFromOnboarding(
            user,
            dto.getIntroTemplate(),
            dto.getOutroTemplate(),
            dto.getTone(),
            dto.getEmojiLevel(),
            dto.getTargetLength()
        );
    }
}