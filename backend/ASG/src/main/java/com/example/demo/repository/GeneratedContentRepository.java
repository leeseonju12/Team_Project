package com.example.demo.repository;

import com.example.demo.entity.GeneratedContent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GeneratedContentRepository extends JpaRepository<GeneratedContent, Long> {
    // 최신순으로 조회
    List<GeneratedContent> findAllByOrderByCreatedAtDesc();
}