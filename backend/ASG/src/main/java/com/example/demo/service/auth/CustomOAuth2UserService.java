package com.example.demo.service.auth;

import com.example.demo.domain.auth.AuthProvider;
import com.example.demo.domain.auth.User;
import com.example.demo.oauth.userinfo.GoogleOAuth2UserInfo;
import com.example.demo.oauth.userinfo.KakaoOAuth2UserInfo;
import com.example.demo.oauth.userinfo.NaverOAuth2UserInfo;
import com.example.demo.oauth.userinfo.OAuth2UserInfo;
import com.example.demo.security.PrincipalDetails;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserAccountService userAccountService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        OAuth2UserInfo userInfo = createOAuth2UserInfo(registrationId, attributes);
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        String providerId = userInfo.getProviderId();
        if (providerId == null || providerId.isBlank()) {
            throw new OAuth2AuthenticationException("소셜 계정 식별자(providerId)가 누락되었습니다.");
        }

        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            if (provider == AuthProvider.KAKAO) {
                throw new OAuth2AuthenticationException("카카오 이메일 동의가 필요합니다. 동의항목을 확인해주세요.");
            }
            throw new OAuth2AuthenticationException("소셜 계정 이메일이 누락되었습니다.");
        }

        User user = userAccountService.saveOrUpdateOAuthUser(
                provider,
                providerId,
                email,
                userInfo.getName()
        );

        log.info("OAuth2 로그인 성공 provider={}, providerId={}, email={}", provider, providerId, email);
        return new PrincipalDetails(user, attributes);
    }

    private OAuth2UserInfo createOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            case "naver" -> new NaverOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 provider 입니다: " + registrationId);
        };
    }
}