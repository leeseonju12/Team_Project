package com.example.demo.oauth.userinfo;

public interface OAuth2UserInfo {

    String getProviderId();

    String getEmail();

    String getName();
}