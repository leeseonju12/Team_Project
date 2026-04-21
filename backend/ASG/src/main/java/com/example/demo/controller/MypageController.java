package com.example.demo.controller;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.myPage.BrandInfoRequest;
import com.example.demo.dto.myPage.ContentSettingsRequest;
import com.example.demo.dto.myPage.ContentSettingsResponse;
import com.example.demo.security.PrincipalDetails;
import com.example.demo.service.myPage.MypageService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    // ── 마이페이지 메인 ─────────────────────────────────────
    @GetMapping
    public String mypage(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        if (principalDetails == null) return "redirect:/login";

        Long userId = principalDetails.getUser().getId();
        model.addAttribute("brandInfo", mypageService.getBrandInfo(userId));
        model.addAttribute("snsAccounts", mypageService.getSnsAccounts(userId));
        model.addAttribute("userInfo", mypageService.getUserInfo(userId));
        return "mypage";
    }

    // ── 가게 정보 수정 ──────────────────────────────────────
    @PostMapping("/brand")
    public String updateBrand(@AuthenticationPrincipal PrincipalDetails principalDetails,
                              @ModelAttribute BrandInfoRequest request) {
        if (principalDetails == null) return "redirect:/login";

        Long userId = principalDetails.getUser().getId();
        mypageService.updateBrandInfo(userId, request);
        return "redirect:/mypage";
    }

    // ── 회원 기본정보 수정 ──────────────────────────────────
    @PostMapping("/member")
    @ResponseBody
    public ResponseEntity<String> updateMember(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam String name,
            @RequestParam String contactPhone) {
        if (principalDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        Long userId = principalDetails.getUser().getId();
        mypageService.updateMemberInfo(userId, name, contactPhone);
        return ResponseEntity.ok("ok");
    }

    // ── 콘텐츠 설정 조회 ────────────────────────────────────
    @GetMapping("/content-settings")
    @ResponseBody
    public ResponseEntity<?> getContentSettings(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        Long userId = principalDetails.getUser().getId();
        ContentSettingsResponse res = mypageService.getContentSettings(userId);
        return res != null ? ResponseEntity.ok(res) : ResponseEntity.noContent().build();
    }

    // ── 콘텐츠 설정 저장 ────────────────────────────────────
    @PostMapping("/content-settings")
    @ResponseBody
    public ResponseEntity<String> updateContentSettings(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody ContentSettingsRequest request) {
        if (principalDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        Long userId = principalDetails.getUser().getId();
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
    public ResponseEntity<List<com.example.demo.dto.myPage.SnsAccountResponse>> getSnsAccounts(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) return ResponseEntity.status(401).build();

        Long userId = principalDetails.getUser().getId();
        return ResponseEntity.ok(mypageService.getSnsAccounts(userId));
    }

    // ── SNS 연동 해제 ───────────────────────────────────────
    @PostMapping("/sns/{brandPlatformId}/disconnect")
    @ResponseBody
    public ResponseEntity<String> disconnectSns(
            @AuthenticationPrincipal PrincipalDetails principalDetails, 
            @PathVariable Long brandPlatformId) {
        if (principalDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

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
    public ResponseEntity<List<Map<String, Object>>> getBusinessHours(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) return ResponseEntity.status(401).build();

        Long userId = principalDetails.getUser().getId();
        return ResponseEntity.ok(mypageService.getBusinessHours(userId));
    }

    // ── 영업시간 저장 ───────────────────────────────────────
    @PostMapping("/business-hours")
    @ResponseBody
    public ResponseEntity<String> updateBusinessHours(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody List<Map<String, Object>> hours) {
        if (principalDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        Long userId = principalDetails.getUser().getId();
        try {
            mypageService.updateBusinessHours(userId, hours);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ── 탈퇴하기 ────────────────────────────────────────────
    @PostMapping("/withdraw")
    public String withdraw(@AuthenticationPrincipal PrincipalDetails principalDetails, HttpSession session) {
        if (principalDetails == null) return "redirect:/login";

        Long userId = principalDetails.getUser().getId();
        mypageService.withdrawUser(userId);
        session.invalidate(); // 탈퇴 시 세션 무효화는 유지
        return "redirect:/landing-page?withdrawn=true";
    }

    // ── 대표이미지 업로드 ────────────────────────────────────
    @PostMapping("/brand/image")
    @ResponseBody
    public ResponseEntity<String> uploadImage(
            @AuthenticationPrincipal PrincipalDetails principalDetails, 
            @RequestParam("file") MultipartFile file) throws IOException {
        if (principalDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        Long userId = principalDetails.getUser().getId();
        String imageUrl = mypageService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(imageUrl);
    }

    // ── 주소 저장 ────────────────────────────────────────────
    @PostMapping("/address")
    @ResponseBody
    public ResponseEntity<String> updateAddress(
            @AuthenticationPrincipal PrincipalDetails principalDetails, 
            @RequestParam String roadAddrPart1,
            @RequestParam String addrDetail) {
        if (principalDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        Long userId = principalDetails.getUser().getId();
        mypageService.updateAddress(userId, roadAddrPart1, addrDetail);
        return ResponseEntity.ok("ok");
    }

    // ── 문의 내역 조회 ──────────────────────────────────────
    @GetMapping("/inquiry")
    @ResponseBody
    public ResponseEntity<List<com.example.demo.dto.myPage.InquiryResponse>> getInquiries(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam String email) {
        if (principalDetails == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(mypageService.getInquiries(email));
    }

    // ── 콘텐츠 히스토리 조회 ────────────────────────────────
    @GetMapping("/content-history")
    @ResponseBody
    public ResponseEntity<List<MypageService.ContentHistoryResponse>> getContentHistory(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) return ResponseEntity.status(401).build();

        Long userId = principalDetails.getUser().getId();
        return ResponseEntity.ok(mypageService.getContentHistory(userId));
    }
}