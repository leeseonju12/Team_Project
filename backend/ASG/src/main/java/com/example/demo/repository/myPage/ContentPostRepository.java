package com.example.demo.repository.myPage;

import com.example.demo.entity.myPage.ContentPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ContentPostRepository extends JpaRepository<ContentPost, Long> {

    // brand_id 기준으로 최신순 조회 (brand_platform → brand 경유)
    @Query("SELECT cp FROM ContentPost cp " +
           "JOIN cp.brandPlatform bp " +
           "JOIN bp.brand b " +
           "WHERE b.brandId = :brandId " +
           "ORDER BY cp.publishedAt DESC")
    List<ContentPost> findByBrandIdOrderByPublishedAtDesc(@Param("brandId") Long brandId);
}