package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // jakarta보다 spring 권장

import com.example.demo.domain.calendar.entity.ContentStatus;
import com.example.demo.entity.GeneratedContent;
import com.example.demo.repository.GeneratedContentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeneratedContentService {

    private final GeneratedContentRepository contentRepository; // 변수명 변경

    @Transactional
    public void updateContentSchedule(Long contentId, String dateStr) {
        // 1. 엔티티 조회 (Post -> GeneratedContent)
        GeneratedContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 컨텐츠가 존재하지 않습니다. ID: " + contentId));

        LocalDateTime scheduledDate;

        // 2. 날짜 파싱 로직
        if (dateStr.contains("T")) {
            String cleanDate = dateStr.split("\\+")[0].split("Z")[0];
            scheduledDate = LocalDateTime.parse(cleanDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            scheduledDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
        }

        // 3. 필드 업데이트 (setScheduledAt -> setScheduledDate)
        content.setScheduledDate(scheduledDate);
        content.setStatus(ContentStatus.SCHEDULED);
        
        // Dirty Checking(변경 감지)에 의해 트랜잭션 종료 시 DB에 반영됩니다.
    }
}