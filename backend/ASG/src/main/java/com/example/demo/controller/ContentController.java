package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.PublishRequest;
import com.example.demo.dto.SnsResult;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.ContentService;
import com.example.demo.service.FacebookApiService;
import com.example.demo.service.ImgbbService;
import com.example.demo.service.InstagramApiService; // 추가됨
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService geminiService;

    
    @GetMapping("/generate")
    public String showGeneratePage() {
        return "content-generate";
    	//return "content-generate-new";
    }

    @PostMapping("/generate")
    public String generate(@ModelAttribute ContentRequest request, Model model) {

        List<SnsResult> results = geminiService.generateAllSnsContent(request);
        
        model.addAttribute("results", results);
        model.addAttribute("req", request); // 입력했던 내용 유지용
        
        return "content-generate";
    }

   
}