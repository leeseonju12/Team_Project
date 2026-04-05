package com.example.demo.repository.myPage;

import com.example.demo.entity.myPage.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    // user 연동 전까지 brandId로 직접 조회
}