package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.ScheduleRequestDto;
import com.example.demo.dto.SnsResult;
import com.example.demo.entity.GeneratedContent;
import com.example.demo.repository.GeneratedContentRepository;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.ContentService;
import com.example.demo.service.FacebookApiService;
import com.example.demo.service.GeneratedContentService;
import com.example.demo.service.InstagramApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ContentApiController {

    private final ContentService contentService;
    private final InstagramApiService instagramService; 
    private final FacebookApiService facebookService;
    private final CloudinaryService cloudinaryService;
    private final GeneratedContentRepository contentRepository;
	private final GeneratedContentService generatedContentService;

	/**
	 * 1. 미배정 컨텐츠 목록 조회
	 */
	@GetMapping("/pending")
	public ResponseEntity<List<GeneratedContent>> getPendingPosts() {
		// 메서드명 변경 반영: findByScheduledAt... -> findByScheduledDate...
		List<GeneratedContent> pendingContents = contentRepository.findByScheduledDateIsNullOrderByCreatedAtDesc();
		return ResponseEntity.ok(pendingContents);
	}

	/**
	 * 2. 캘린더 일정 데이터 조회 (FullCalendar 포맷)
	 */
	@GetMapping("/events")
	public List<Map<String, Object>> getCalendarEvents() {
		return contentRepository.findByScheduledDateIsNotNull().stream().map(content -> {
			Map<String, Object> event = new HashMap<>();
			event.put("id", content.getId());
			event.put("title", content.getMenuName()); // post.getTitle() -> content.getMenuName()
			event.put("start", content.getScheduledDate()); // post.getScheduledAt() -> content.getScheduledDate()

			// 기존 borderColor 필드가 스키마에서 삭제되었으므로, 필요시 하드코딩하거나 플랫폼별 컬러 로직 추가
			event.put("color", "#3788d8");

			Map<String, Object> extendedProps = new HashMap<>();
			extendedProps.put("content", content.getContent());
			extendedProps.put("platform", content.getPlatform());
			extendedProps.put("hashtags", content.getHashtags()); // 새 필드 추가

			event.put("extendedProps", extendedProps);
			return event;
		}).collect(Collectors.toList());
	}

//	/**
//	 * 3. 일정 저장 및 변경 (드래그 앤 드롭)
//	 */
//	@PostMapping("/schedule")
//	public ResponseEntity<?> updateSchedule(@RequestBody ScheduleRequestDto request) {
//		try {
//			// DTO 필드명 contentId 및 서비스 메서드명 변경 반영
//			generatedContentService.updateContentSchedule(request.getContentId(), request.getScheduledDate());
//			return ResponseEntity.ok(Map.of("success", true, "message", "일정이 업데이트되었습니다."));
//		} catch (Exception e) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body(Map.of("success", false, "message", e.getMessage()));
//		}
//	}
	
	@PutMapping("/{contentId}/schedule")
	public ResponseEntity<?> updateSchedule(
	        @PathVariable Long contentId,
	        @RequestBody ScheduleRequestDto request) {
	    try {
	        // 식별자는 URL Path에서, 수정할 날짜 데이터는 Request Body에서 추출하여 서비스 메서드 호출
	        generatedContentService.updateContentSchedule(contentId, request.getScheduledDate());
	        
	        return ResponseEntity.ok(Map.of("success", true, "message", "일정이 업데이트되었습니다."));
	    } catch (IllegalArgumentException e) {
	        // 리소스를 찾을 수 없거나 유효하지 않은 요청일 경우 400 상태 코드 반환
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("success", false, "message", e.getMessage()));
	    } catch (Exception e) {
	        // 그 외 서버 에러 발생 시 500 상태 코드 반환
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("success", false, "message", "일정 처리 중 서버 오류가 발생했습니다."));
	    }
	}

	// --- 기존 SNS 콘텐츠 생성 및 발행 로직 ---

	@PostMapping("/generate")

	public Map<String, String> generateForApi(@RequestBody ContentRequest request) {
		List<SnsResult> results = contentService.generateAllSnsContent(request);
		return results.stream().collect(Collectors.toMap(
				res -> res.getPlatform().toLowerCase(),
				res -> res.getContent()
		));
	}

	@PostMapping("/publish")

	public Map<String, Object> publishToSns(
			@RequestParam("platform") String platform,
			@RequestParam("text") String text,
			@RequestParam(value = "image", required = false) MultipartFile imageFile) {
		try {

			String realImageUrl = null;
			if (imageFile != null && !imageFile.isEmpty()) {
				realImageUrl = cloudinaryService.uploadImage(imageFile);
			}

			if ("instagram".equalsIgnoreCase(platform)) {
				if (realImageUrl == null)
					throw new RuntimeException("인스타그램은 이미지가 필수입니다.");
				instagramService.publishPost(realImageUrl, text);
				return Map.of("status", "success", "message", "Instagram 게시 완료!");
			} else if ("facebook".equalsIgnoreCase(platform)) {
				if (realImageUrl == null)
					throw new RuntimeException("페이스북 /photos API는 이미지가 필수입니다.");
				facebookService.publishPost(realImageUrl, text);
				return Map.of("status", "success", "message", "Facebook 게시 완료!");
			}

			return Map.of("status", "fail", "message", "지원하지 않는 플랫폼입니다.");

		} catch (Exception e) {
            System.err.println("[" + platform + "] 발행 실패: " + e.getMessage());
            return Map.of("status", "fail", "message", e.getMessage());
        }
    }
	
	@PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = cloudinaryService.uploadImage(file);
        
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        
        return ResponseEntity.ok(response);
    }
}