package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.example.demo.calendar.entity.Post;
import com.example.demo.calendar.entity.PostStatus;
import com.example.demo.repository.PostRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public void updatePostSchedule(Long postId, String dateStr) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 포스트가 존재하지 않습니다. ID: " + postId));

        LocalDateTime scheduledDate;

        // 1. FullCalendar가 "2026-04-08T12:30:00" 처럼 시/분/초를 보낼 때
        if (dateStr.contains("T")) {
            // 타임존(+09:00 등)이 붙어있을 경우를 대비해 자르기 처리
            String cleanDate = dateStr.split("\\+")[0].split("Z")[0];
            scheduledDate = LocalDateTime.parse(cleanDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } 
        // 2. FullCalendar가 "2026-04-08" 처럼 날짜만 보낼 때
        else {
            scheduledDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
        }
        post.setScheduledAt(scheduledDate);
        post.setStatus(PostStatus.SCHEDULED);
        
        // Dirty Checking으로 자동 저장됩니다.
    }
}