package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.SnsResult;
import com.example.demo.service.ContentService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService geminiService;

    @GetMapping("/generate")
    public String showGeneratePage() {
        return "index";
    }

    @PostMapping("/generate")
    public String generate(HttpSession session,
                           @ModelAttribute ContentRequest request, Model model) {

        Long userId = (Long) session.getAttribute("userId");
        request.setUserId(userId); // null이면 ContentService에서 BRAND_ID=1L로 fallback

        List<SnsResult> results = geminiService.generateAllSnsContent(request);
        
        model.addAttribute("results", results);
        model.addAttribute("req", request);
        
        return "index";
    }
}