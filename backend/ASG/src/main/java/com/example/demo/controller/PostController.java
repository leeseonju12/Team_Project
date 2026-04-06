package com.example.demo.controller;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.calendar.entity.Post;
import com.example.demo.dto.ScheduleRequestDto;
import com.example.demo.service.PostService;
import com.example.demo.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Controller // 템플릿 반환을 위해 @Controller 사용
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;

    /**
     * 1. 캘린더 메인 페이지 반환 (Thymeleaf)
     */
    @GetMapping
    public String calendarPage(Model model) {
        // 미배정 포스트를 서버 사이드에서 바로 전달 (Thymeleaf th:each 사용용)
        List<Post> pendingPosts = postRepository.findByScheduledAtIsNullOrderByCreatedAtDesc();
        model.addAttribute("pendingPosts", pendingPosts);
        return "calendar-test"; // resources/templates/calendar_template.html
    }

    /**
     * 2. 캘린더에 표시할 일정 데이터 (JSON API)
     * FullCalendar의 'events: /calendar/events' 설정과 연동
     */
    @GetMapping("/events")
    @ResponseBody // JSON 반환
    public List<Map<String, Object>> getCalendarEvents() {
        return postRepository.findByScheduledAtIsNotNull().stream().map(post -> {
            Map<String, Object> event = new HashMap<>();
            event.put("id", post.getId());
            event.put("title", post.getTitle());
            event.put("start", post.getScheduledAt()); // ISO8601 포맷 자동 변환
            event.put("color", post.getBorderColor()); // DB에 저장된 색상 적용
            
            // 상세 정보를 담는 객체
            Map<String, Object> extendedProps = new HashMap<>();
            extendedProps.put("content", post.getContent());
            extendedProps.put("platform", post.getPlatform());
            
            event.put("extendedProps", extendedProps);
            return event;
        }).collect(Collectors.toList());
    }

    /**
     * 3. 드래그앤드롭 및 일정 수정 (JSON API)
     */
    @PostMapping("/schedule")
    @ResponseBody
    public ResponseEntity<?> updateSchedule(@RequestBody ScheduleRequestDto request) {
        // 1. 요청 데이터 검증
        if (request.getPostId() == null || request.getScheduledDate() == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "필수 데이터가 누락되었습니다."));
        }

        try {
            postService.updatePostSchedule(request.getPostId(), request.getScheduledDate());
            
            return ResponseEntity.ok(Map.of("success", true, "message", "일정이 업데이트되었습니다."));

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "날짜 형식이 올바르지 않습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }
}