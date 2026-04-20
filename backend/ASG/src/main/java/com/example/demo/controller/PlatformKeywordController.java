package com.example.demo.controller;

import com.example.demo.dto.channel.PlatformKeywordResponseDto;
import com.example.demo.service.PlatformKeywordService;
import com.example.demo.service.myPage.MypageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class PlatformKeywordController {

    private final PlatformKeywordService platformKeywordService;
    private final MypageService mypageService;

    @GetMapping("/keywords")
    public ResponseEntity<?> getKeywords(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        Long brandId = mypageService.getBrandId(userId);
        return ResponseEntity.ok(platformKeywordService.getKeywords(brandId));
    }
}

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 검색어 노드를 띄우기 위한 호출
브랜드 아이디 하드코딩 중

*/