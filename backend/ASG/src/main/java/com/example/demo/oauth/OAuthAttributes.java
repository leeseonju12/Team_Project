package com.example.demo.oauth;

import com.example.demo.domain.auth.AuthProvider;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthAttributes {

    private final AuthProvider provider;
    private final String providerId;
    private final String email;
    private final String name;
    private final Map<String, Object> attributes;

    @Builder
    private OAuthAttributes(AuthProvider provider, String providerId, String email, String name,
                            Map<String, Object> attributes) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.attributes = attributes;
    }

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            case "naver" -> ofNaver(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth provider: " + registrationId);
        };
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .provider(AuthProvider.GOOGLE)
                .providerId(String.valueOf(attributes.get("sub")))
                .email((String) attributes.get("email"))
                .name((String) attributes.getOrDefault("name", "Google User"))
                .attributes(attributes)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());

        return OAuthAttributes.builder()
                .provider(AuthProvider.KAKAO)
                .providerId(String.valueOf(attributes.get("id")))
                .email((String) account.get("email"))
                .name((String) profile.getOrDefault("nickname", "Kakao User"))
                .attributes(attributes)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.getOrDefault("response", Map.of());

        return OAuthAttributes.builder()
                .provider(AuthProvider.NAVER)
                .providerId((String) response.get("id"))
                .email((String) response.get("email"))
                .name((String) response.getOrDefault("name", "Naver User"))
                .attributes(attributes)
                .build();
    }
}