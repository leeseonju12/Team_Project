package com.example.demo.controller;

import com.example.demo.dto.myPage.BrandInfoRequest;
import com.example.demo.dto.myPage.ContentSettingsRequest;
import com.example.demo.service.myPage.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}