package com.example.demo.repository.myPage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.myPage.BrandPlatform;

public interface BrandPlatformRepository extends JpaRepository<BrandPlatform, Long> {

    List<BrandPlatform> findByBrand_BrandId(Long brandId);
}
