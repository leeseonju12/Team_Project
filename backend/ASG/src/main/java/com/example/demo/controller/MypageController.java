package com.example.demo.controller;

import com.example.demo.dto.myPage.BrandInfoRequest;
import com.example.demo.dto.myPage.ContentSettingsRequest;
import com.example.demo.dto.myPage.ContentSettingsResponse;
import com.example.demo.service.myPage.MypageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

	private final MypageService mypageService;

	/** 세션에서 userId 추출. 없으면 null 반환 */
	private Long getSessionUserId(HttpSession session) {
		return (Long) session.getAttribute("userId");
	}

    // ── 마이페이지 메인 ─────────────────────────────────────
    @GetMapping
    public String mypage(HttpSession session, Model model) {
        Long userId = getSessionUserId(session);
        if (userId == null) return "redirect:/login";

		model.addAttribute("brandInfo", mypageService.getBrandInfo(userId));
		model.addAttribute("snsAccounts", mypageService.getSnsAccounts(userId));
		model.addAttribute("userInfo", mypageService.getUserInfo(userId));
		return "mypage";
	}

	// ── 가게 정보 수정 ──────────────────────────────────────
	@PostMapping("/brand")
	public String updateBrand(HttpSession session, @ModelAttribute BrandInfoRequest request) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return "redirect:/login_test";
		mypageService.updateBrandInfo(userId, request);
		return "redirect:/mypage";
	}

	// ── 회원 기본정보 수정 ──────────────────────────────────
	@PostMapping("/member")
	@ResponseBody
	public ResponseEntity<String> updateMember(
	        HttpSession session,
	        @RequestParam String name,
	        @RequestParam String contactPhone) {
	    Long userId = getSessionUserId(session);
	    if (userId == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
	    mypageService.updateMemberInfo(userId, name, contactPhone);
	    return ResponseEntity.ok("ok");
	}

	// ── 콘텐츠 설정 조회 ────────────────────────────────────
	@GetMapping("/content-settings")
	@ResponseBody
	public ResponseEntity<?> getContentSettings(HttpSession session) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).body("로그인이 필요합니다.");
		ContentSettingsResponse res = mypageService.getContentSettings(userId);
		return res != null ? ResponseEntity.ok(res) : ResponseEntity.noContent().build();
	}

	// ── 콘텐츠 설정 저장 ────────────────────────────────────
	@PostMapping("/content-settings")
	@ResponseBody
	public ResponseEntity<String> updateContentSettings(HttpSession session,
			@RequestBody ContentSettingsRequest request) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).body("로그인이 필요합니다.");
		try {
			mypageService.updateContentSettings(userId, request);
			return ResponseEntity.ok("ok");
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}

	// ── SNS 연동 목록 조회 ──────────────────────────────────
	@GetMapping("/sns/list")
	@ResponseBody
	public ResponseEntity<List<com.example.demo.dto.myPage.SnsAccountResponse>> getSnsAccounts(HttpSession session) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).build();
		return ResponseEntity.ok(mypageService.getSnsAccounts(userId));
	}

	// ── SNS 연동 해제 ───────────────────────────────────────
	@PostMapping("/sns/{brandPlatformId}/disconnect")
	@ResponseBody
	public ResponseEntity<String> disconnectSns(HttpSession session, @PathVariable Long brandPlatformId) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).body("로그인이 필요합니다.");
		try {
			mypageService.disconnectSns(brandPlatformId);
			return ResponseEntity.ok("ok");
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}

	// ── 영업시간 조회 ───────────────────────────────────────
	@GetMapping("/business-hours")
	@ResponseBody
	public ResponseEntity<List<Map<String, Object>>> getBusinessHours(HttpSession session) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).build();
		return ResponseEntity.ok(mypageService.getBusinessHours(userId));
	}

	// ── 영업시간 저장 ───────────────────────────────────────
	@PostMapping("/business-hours")
	@ResponseBody
	public ResponseEntity<String> updateBusinessHours(HttpSession session,
			@RequestBody List<Map<String, Object>> hours) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).body("로그인이 필요합니다.");
		try {
			mypageService.updateBusinessHours(userId, hours);
			return ResponseEntity.ok("ok");
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}

	// ── 탈퇴하기 ────────────────────────────────────────────
	@PostMapping("/withdraw")
	public String withdraw(HttpSession session) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return "redirect:/login_test";
		mypageService.withdrawUser(userId);
		session.invalidate();
		return "redirect:/landing-page?withdrawn=true";
	}

	// ── 대표이미지 업로드 ────────────────────────────────────
	@PostMapping("/brand/image")
	@ResponseBody
	public ResponseEntity<String> uploadImage(HttpSession session, @RequestParam("file") MultipartFile file)
			throws IOException {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).body("로그인이 필요합니다.");
		String imageUrl = mypageService.uploadProfileImage(userId, file);
		return ResponseEntity.ok(imageUrl);
	}

	// ── 주소 저장 ────────────────────────────────────────────
	@PostMapping("/address")
	@ResponseBody
	public ResponseEntity<String> updateAddress(HttpSession session, @RequestParam String roadAddrPart1,
			@RequestParam String addrDetail) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).body("로그인이 필요합니다.");
		mypageService.updateAddress(userId, roadAddrPart1, addrDetail);
		return ResponseEntity.ok("ok");
	}

	// ── 문의 내역 조회 ──────────────────────────────────────
	@GetMapping("/inquiry")
	@ResponseBody
	public ResponseEntity<List<com.example.demo.dto.myPage.InquiryResponse>> getInquiries(HttpSession session,
			@RequestParam String email) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).build();
		return ResponseEntity.ok(mypageService.getInquiries(email));
	}

	// ── 콘텐츠 히스토리 조회 ────────────────────────────────
	@GetMapping("/content-history")
	@ResponseBody
	public ResponseEntity<List<MypageService.ContentHistoryResponse>> getContentHistory(HttpSession session) {
		Long userId = getSessionUserId(session);
		if (userId == null)
			return ResponseEntity.status(401).build();
		return ResponseEntity.ok(mypageService.getContentHistory(userId));
	}
}