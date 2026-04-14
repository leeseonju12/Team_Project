package com.example.demo.repository.myPage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.myPage.BrandOperationProfile;

public interface BrandOperationProfileRepository extends JpaRepository<BrandOperationProfile, Long> {

    Optional<BrandOperationProfile> findByBrand_BrandId(Long brandId);
}
