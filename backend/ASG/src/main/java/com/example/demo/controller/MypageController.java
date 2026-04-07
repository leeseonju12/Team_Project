package com.example.demo.controller;

import com.example.demo.dto.myPage.BrandInfoRequest;
import com.example.demo.dto.myPage.ContentSettingsRequest;
import com.example.demo.service.myPage.MypageService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    // ── 마이페이지 메인 ─────────────────────────────────────
    @GetMapping
    public String mypage(Model model) {
        model.addAttribute("brandInfo", mypageService.getBrandInfo());
        //model.addAttribute("contentSettings", mypageService.getContentSettings());
        model.addAttribute("snsAccounts", mypageService.getSnsAccounts());
        model.addAttribute("userInfo", mypageService.getUserInfo());
        model.addAttribute("brandName", mypageService.getBrandName());
        return "mypage";
    }

    // ── 가게 정보 수정 ──────────────────────────────────────
    @PostMapping("/brand")
    public String updateBrand(@ModelAttribute BrandInfoRequest request) {
        mypageService.updateBrandInfo(request);
        return "redirect:/mypage";
    }

    // ── 콘텐츠 설정 수정 ────────────────────────────────────
    /*@PostMapping("/content-settings")
    public String updateContentSettings(@ModelAttribute ContentSettingsRequest request) {
        mypageService.updateContentSettings(request);
        return "redirect:/mypage";
    }*/

    // ── SNS 연동 해제 ───────────────────────────────────────
    @PostMapping("/sns/{brandPlatformId}/disconnect")
    public String disconnectSns(@PathVariable Long brandPlatformId) {
        mypageService.disconnectSns(brandPlatformId);
        return "redirect:/mypage";
    }
    
	// ── 영업 시간대 설정 ───────────────────────────────────────
    @PostMapping("/business-hours")
    @ResponseBody
    public ResponseEntity<String> updateBusinessHours (
        @RequestBody List<Map<String, Object>> hours) {
        // 일단 데이터 확인용으로 로그만 출력
        System.out.println("영업시간 수신: " + hours);
        return ResponseEntity.ok("ok");
    }
    
 // ── 탈퇴하기 ───────────────────────────────────────
    @PostMapping("/withdraw")
    public String withdraw() {
        mypageService.withdrawUser();
        return "redirect:/landing-page?withdrawn=true";
    }
}
