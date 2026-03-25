package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.springframework.stereotype.Service;

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
        // 1. 모든 JSON에서 텍스트(제목, 설명)만 추출해서 하나로 합치기
        StringBuilder combinedText = new StringBuilder();
        
        for (String json : jsonResponses) {
            combinedText.append(extractTextFromJson(json));
        }

        // 2. KOMORAN으로 명사 추출하기
        KomoranResult analyzeResultList = komoran.analyze(combinedText.toString());
        List<String> nouns = analyzeResultList.getNouns(); // 명사만 쏙 뽑기

        // 3. 단어별 빈도수 계산하기
        Map<String, Integer> wordCounts = new HashMap<>();
        for (String noun : nouns) {
            // 2글자 이상인 단어만 필터링 (의미 없는 한 글자 단어 제외)
            if (noun.length() > 1) {
                wordCounts.put(noun, wordCounts.getOrDefault(noun, 0) + 1);
            }
        }

        // 4. 빈도수 기준 내림차순 정렬 후 상위 4개 선정
        return wordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(4) // 💡 마인드맵 가지 수에 맞춰 4개로 제한
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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