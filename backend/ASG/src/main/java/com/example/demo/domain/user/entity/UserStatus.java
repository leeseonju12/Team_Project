package com.example.demo.domain.user.entity;

public enum UserStatus {
    SIGNUP_PENDING,  // 소셜 로그인 완료, 회원가입 미완료
    ACTIVE           // 회원가입 완료, 정상 사용자
}