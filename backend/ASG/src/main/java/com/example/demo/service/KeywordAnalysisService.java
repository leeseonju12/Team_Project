package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeywordAnalysisService {

    private final Komoran komoran;
    private final ObjectMapper objectMapper;

    public KeywordAnalysisService() {
        // 💡 KOMORAN 모델 초기화 (메모리 효율을 위해 LIGHT 모델 사용)
        this.komoran = new Komoran(DEFAULT_MODEL.LIGHT);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * @param rawData: API에서 받은 데이터 리스트 (JSON 문자열 또는 일반 텍스트)
     * @return: 가장 많이 등장한 명사 상위 4개
     */
    public List<String> analyzeKeywords(List<String> rawData) {
        StringBuilder combinedText = new StringBuilder();

        for (String data : rawData) {
            if (data == null || data.trim().isEmpty()) continue;

            // 1️⃣ 데이터가 JSON 형태({로 시작)인지 확인합니다.
            if (data.trim().startsWith("{")) {
                // 유튜브, 네이버 등 JSON 응답은 파싱하여 텍스트만 추출
                combinedText.append(extractTextFromJson(data)).append(" ");
            } else {
                // 구글, 인스타그램 등 이미 추출된 일반 텍스트는 그대로 추가
                combinedText.append(data).append(" ");
            }
        }

        // 2️⃣ 텍스트가 비어있으면 빈 리스트 반환
        String finalEfText = combinedText.toString().trim();
        if (finalEfText.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 3️⃣ 형태소 분석 (명사만 추출)
            KomoranResult analyzeResultList = komoran.analyze(finalEfText);
            List<String> nouns = analyzeResultList.getNouns();

            // 4️⃣ 빈도수 계산 및 상위 4개 추출
            Map<String, Long> frequencyMap = nouns.stream()
                    .filter(noun -> noun.length() > 1) // 1글자 단어 제외
                    .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

            return frequencyMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(4)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("====> 분석 중 오류 발생: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * JSON 구조에서 제목(title)과 내용(description/snippet) 텍스트만 뽑아내는 보조 메서드
     */
    private String extractTextFromJson(String json) {
        StringBuilder sb = new StringBuilder();
        try {
            JsonNode root = objectMapper.readTree(json);
            
            // SerpApi(results), YouTube/Naver(items) 대응
            JsonNode items = root.has("items") ? root.get("items") : root.get("organic_results");

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    // YouTube 대응 (snippet 노드 존재 시)
                    if (item.has("snippet") && item.get("snippet").isObject()) {
                        JsonNode snippet = item.get("snippet");
                        if (snippet.has("title")) sb.append(snippet.get("title").asText()).append(" ");
                        if (snippet.has("description")) sb.append(snippet.get("description").asText()).append(" ");
                    } 
                    // 일반적인 구조 (title, snippet, description)
                    else {
                        if (item.has("title")) sb.append(item.get("title").asText()).append(" ");
                        if (item.has("snippet")) sb.append(item.get("snippet").asText()).append(" ");
                        if (item.has("description")) sb.append(item.get("description").asText()).append(" ");
                    }
                }
            }
        } catch (Exception e) {
            // JSON 형식이 아니거나 파싱 실패 시 원문 그대로 반환 시도
            return json;
        }
        return sb.toString();
    }
}