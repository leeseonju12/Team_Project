package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import kr.co.shineware.nlp.komoran.model.Token;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeywordAnalysisService {

    private final Komoran komoran;
    private final ObjectMapper objectMapper;

    public KeywordAnalysisService() {
        // 💡 KOMORAN 모델 초기화 (LIGHT 모델이 메모리를 적게 먹습니다)
        this.komoran = new Komoran(DEFAULT_MODEL.LIGHT);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * @param jsonResponses: API에서 받은 JSON 문자열 리스트 (100개분)
     * @return: 가장 많이 등장한 명사 상위 4개
     */
    public List<String> analyzeKeywords(List<String> jsonResponses) {
        StringBuilder combinedText = new StringBuilder();
        for (String json : jsonResponses) {
            combinedText.append(extractTextFromJson(json));
        }

        String text = combinedText.toString();
        if (text.isBlank()) return new ArrayList<>();  // 빈 텍스트 방어

        Map<String, Integer> wordCounts = new HashMap<>();

        // 1. 해시태그 직접 추출 (가중치 3배)
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("#([\\w가-힣]{2,})").matcher(text);
        while (matcher.find()) {
            String tag = matcher.group(1);
            if (!isStopWord(tag)) {
                wordCounts.put(tag, wordCounts.getOrDefault(tag, 0) + 3);
            }
        }

        // 2. Komoran getNouns() 로 명사 추출
        try {
            List<String> nouns = komoran.analyze(text).getNouns();
            for (int i = 0; i < nouns.size(); i++) {
                String noun = nouns.get(i);
                if (noun.length() < 2 || isStopWord(noun)) continue;

                // 연속된 명사 이어붙이기
                if (i + 1 < nouns.size()) {
                    String compound = noun + nouns.get(i + 1);
                    if (!isStopWord(compound)) {
                        wordCounts.put(compound, wordCounts.getOrDefault(compound, 0) + 2);
                    }
                }
                wordCounts.put(noun, wordCounts.getOrDefault(noun, 0) + 1);
            }
        } catch (Exception e) {
            // Komoran 오류 시 해시태그 결과만 반환
        }

        return wordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private boolean isStopWord(String word) {
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "성심", "성심당", "sungsimdang",
            "여행", "방문", "소개", "추천", "리뷰", "영상", "동영상",
            "이번", "오늘", "최근", "정말", "진짜", "너무", "매우",
            "가게", "매장", "브랜드", "제품", "상품", "구매", "판매",
            "블로그", "인스타", "유튜브", "네이버", "구글",
            "사진", "포스팅", "게시물", "댓글", "좋아요",
            "이용", "서비스", "이벤트", "할인", "쿠폰"
        ));
        return stopWords.contains(word);
    }

    /**
     * JSON 구조에서 제목(title)과 내용(description) 텍스트만 뽑아내는 보조 메서드
     */
    private String extractTextFromJson(String json) {
        StringBuilder sb = new StringBuilder();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.has("items") ? root.get("items") : root.get("results");

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {

                    // ✅ YouTube: title/description이 snippet 안에 있음
                    if (item.has("snippet") && item.get("snippet").isObject()) {
                        JsonNode snippet = item.get("snippet");
                        if (snippet.has("title"))       sb.append(snippet.get("title").asText()).append(" ");
                        if (snippet.has("description")) sb.append(snippet.get("description").asText()).append(" ");

                    // ✅ Google/Naver: title, snippet이 바로 item 아래에 있음
                    } else {
                        if (item.has("title"))       sb.append(item.get("title").asText()).append(" ");
                        if (item.has("snippet"))     sb.append(item.get("snippet").asText()).append(" ");
                        if (item.has("description")) sb.append(item.get("description").asText()).append(" ");
                    }
                }
            }
        } catch (Exception e) {
            // 파싱 실패 시 무시
        }
        return sb.toString();
    }
}