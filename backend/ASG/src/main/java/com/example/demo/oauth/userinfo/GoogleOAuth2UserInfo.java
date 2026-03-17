package com.example.demo.oauth.userinfo;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return normalize(attributes.get("sub"));
    }

    @Override
    public String getEmail() {
        return normalize(attributes.get("email"));
    }

    @Override
    public String getName() {
        String name = normalize(attributes.get("name"));
        return name == null ? "Google User" : name;
    }

    private String normalize(Object value) {
        if (value == null) {
            return null;
        }
        String normalized = String.valueOf(value).trim();
        if (normalized.isEmpty() || "null".equalsIgnoreCase(normalized)) {
            return null;
        }
        return normalized;
    }
}