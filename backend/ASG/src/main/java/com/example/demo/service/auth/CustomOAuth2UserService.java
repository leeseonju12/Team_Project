package com.example.demo.service.auth;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.domain.user.entity.User;
import com.example.demo.oauth.userinfo.OAuth2UserInfo;
import com.example.demo.oauth.userinfo.OAuth2UserInfoFactory;
import com.example.demo.repository.auth.UserRepository;
import com.example.demo.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());
        User user = saveOrUpdateUser(userInfo);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("userId", user.getId());
        attributes.put("userStatus", user.getStatus().name());
        attributes.put("provider", provider);

        return new PrincipalDetails(user, attributes);
    }

    private User saveOrUpdateUser(OAuth2UserInfo userInfo) {
        return userRepository
            .findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
            .map(existingUser -> {
                existingUser.updateSocialInfo(userInfo.getEmail(), userInfo.getName());
                log.info("기존 사용자 로그인: provider={}, status={}",
                         userInfo.getProvider(), existingUser.getStatus());
                return existingUser;
            })
            .orElseGet(() -> {
                // 정적 팩토리 메서드로 신규 사용자 생성 ← 변경
                User newUser = User.createSocialUser(
                    userInfo.getProvider(),
                    userInfo.getProviderId(),
                    userInfo.getEmail(),
                    userInfo.getName()
                );
                log.info("신규 사용자 임시 저장: provider={}, email={}",
                         userInfo.getProvider(), userInfo.getEmail());
                return userRepository.save(newUser);
            });
    }
}