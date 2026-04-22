package com.example.demo.controller;

import com.example.demo.entity.Keyword;
import com.example.demo.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordRepository keywordRepository;

    @GetMapping
    public ResponseEntity<List<Keyword>> getKeywordsByIndustry(@RequestParam String industryCode) {

        List<Keyword> keywords = keywordRepository.findByIndustryCode(industryCode);
        return ResponseEntity.ok(keywords);
    }
}