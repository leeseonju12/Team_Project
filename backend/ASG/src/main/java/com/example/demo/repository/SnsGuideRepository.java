package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.SnsGuide;

public interface SnsGuideRepository extends JpaRepository<SnsGuide, Long> {
    // JpaRepository 기본 제공 메서드(findAll) 사용
}