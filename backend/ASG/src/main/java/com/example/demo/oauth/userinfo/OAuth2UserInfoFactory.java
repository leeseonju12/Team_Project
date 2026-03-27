package com.example.demo.oauth.userinfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao"  -> new KakaoOAuth2UserInfo(attributes);
            case "naver"  -> new NaverOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
        };
    }

    // Google
    static class GoogleOAuth2UserInfo implements OAuth2UserInfo {
        private final Map<String, Object> attributes;
        GoogleOAuth2UserInfo(Map<String, Object> attributes) { this.attributes = attributes; }

        @Override public String getProviderId() { return (String) attributes.get("sub"); }
        @Override public String getProvider()   { return "google"; }
        @Override public String getEmail()      { return (String) attributes.get("email"); }
        @Override public String getName()       { return (String) attributes.get("name"); }
    }

    // Kakao
    @SuppressWarnings("unchecked")
    static class KakaoOAuth2UserInfo implements OAuth2UserInfo {
        private final Map<String, Object> attributes;
        KakaoOAuth2UserInfo(Map<String, Object> attributes) { this.attributes = attributes; }

        @Override public String getProviderId() { return String.valueOf(attributes.get("id")); }
        @Override public String getProvider()   { return "kakao"; }
        @Override public String getEmail() {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        }
        @Override public String getName() {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount == null) return null;
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            return profile != null ? (String) profile.get("nickname") : null;
        }
    }

    // Naver
    @SuppressWarnings("unchecked")
    static class NaverOAuth2UserInfo implements OAuth2UserInfo {
        private final Map<String, Object> attributes;
        NaverOAuth2UserInfo(Map<String, Object> attributes) { this.attributes = attributes; }

        private Map<String, Object> getResponse() {
            return (Map<String, Object>) attributes.get("response");
        }
        @Override public String getProviderId() { return (String) getResponse().get("id"); }
        @Override public String getProvider()   { return "naver"; }
        @Override public String getEmail()      { return (String) getResponse().get("email"); }
        @Override public String getName()       { return (String) getResponse().get("name"); }
    }
}