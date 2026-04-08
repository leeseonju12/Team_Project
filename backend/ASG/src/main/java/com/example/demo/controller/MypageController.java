package com.example.demo.controller;

import com.example.demo.dto.myPage.BrandInfoRequest;
import com.example.demo.dto.myPage.ContentSettingsRequest;
import com.example.demo.service.myPage.MypageService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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

 // ── 콘텐츠 설정 조회 ────────────────────────────────────
    @GetMapping("/content-settings")
    @ResponseBody
    public ResponseEntity<?> getContentSettings() {
        com.example.demo.dto.myPage.ContentSettingsResponse res = mypageService.getContentSettings();
        return res != null ? ResponseEntity.ok(res) : ResponseEntity.noContent().build();
    }

    // ── 콘텐츠 설정 저장 ────────────────────────────────────
    @PostMapping("/content-settings")
    @ResponseBody
    public ResponseEntity<String> updateContentSettings(
            @RequestBody com.example.demo.dto.myPage.ContentSettingsRequest request) {
        try {
            mypageService.updateContentSettings(request);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ── SNS 연동 해제 ───────────────────────────────────────
    /*@PostMapping("/sns/{brandPlatformId}/disconnect")
    public String disconnectSns(@PathVariable Long brandPlatformId) {
        mypageService.disconnectSns(brandPlatformId);
        return "redirect:/mypage";
    }*/
    
    // ── 영업 시간대 설정 ───────────────────────────────────────
    @PostMapping("/business-hours")
    @ResponseBody
    public ResponseEntity<String> updateBusinessHours(
        @RequestBody List<Map<String, Object>> hours) {
        try {
            mypageService.updateBusinessHours(hours);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    
    // ── 영업시간 조회 ───────────────────────────────────────
    @GetMapping("/business-hours")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getBusinessHours() {
        return ResponseEntity.ok(mypageService.getBusinessHours());
    }
    
    // ── 탈퇴하기 ───────────────────────────────────────
    @PostMapping("/withdraw")
    public String withdraw() {
        mypageService.withdrawUser();
        return "redirect:/landing-page?withdrawn=true";
    }
    
    // ── 대표이미지 ───────────────────────────────────────
    @PostMapping("/brand/image")
    @ResponseBody
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = mypageService.uploadProfileImage(file);
        return ResponseEntity.ok(imageUrl);
    }
    
    // ── 주소 저장 ───────────────────────────────────────────
    @PostMapping("/address")
    @ResponseBody
    public ResponseEntity<String> updateAddress(
            @RequestParam String roadAddrPart1,
            @RequestParam String addrDetail) {
        mypageService.updateAddress(roadAddrPart1, addrDetail);
        return ResponseEntity.ok("ok");
    }
    
    
}
