package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeywordAnalysisService {

    private final ObjectMapper objectMapper;

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        // 육하원칙
        "누가", "누구", "언제", "어디", "어디서", "어디에", "무엇", "무슨", "왜", "어떻게", "어떤",
        // 인사·감탄
        "반갑습니다", "안녕하세요", "감사합니다", "감사해요", "고맙습니다", "고마워요",
        "안녕", "반가워", "반가워요", "환영합니다",
        // 동사형 (먹기·가기 등 ~기 형태)
        "먹기", "가기", "보기", "찾기", "알기", "쓰기", "받기", "주기", "하기", "되기",
        "사기", "팔기", "열기", "닫기", "오기", "서기",
        // 동사 활용형
        "있는", "있어", "있습니다", "없는", "없어", "없습니다", "있었", "없었",
        "하는", "하고", "하지", "하면", "해서", "했습니다", "합니다", "했어요", "해요", "할게요",
        "이라", "이고", "이며", "이지", "이란", "이에요", "입니다", "이었",
        // 접속·부사
        "때문", "그리고", "그래서", "그런데", "하지만", "또한", "그냥", "바로", "물론",
        "역시", "아마", "혹시", "만약", "비록", "드디어", "갑자기", "결국",
        // 시간
        "이번", "지난", "오늘", "어제", "내일", "최근", "앞으로", "지금", "아직",
        "항상", "매일", "자주", "가끔", "요즘", "요새",
        // 정도·수량
        "정말", "너무", "많이", "조금", "아주", "매우", "더욱", "굉장히", "엄청", "완전",
        "모두", "모든", "각각", "여러", "다양한", "가장", "더", "제일",
        // 평가형 (의미 없는 형용사)
        "좋은", "좋아", "좋습니다", "좋아요", "나쁜", "싫은",
        "맛있는", "맛있어", "맛있습니다", "맛있어요",
        "예쁜", "귀여운", "멋진", "화려한",
        // 일반 동작 동사
        "가능", "필요", "관련", "경우", "통해", "위해", "대한", "따른",
        "진행", "운영", "제공", "확인", "방문", "이용", "사용", "선택",
        "소개", "공유", "업로드", "다운로드", "클릭", "검색",
        // HTML 태그·URL 잔재
        "https", "http", "www", "com", "co", "kr", "html", "php",
        "strong", "nbsp", "amp",
        // 기타 형식어
        "등등", "등을", "것이", "것을", "것은", "것도", "수도", "수는", "수를",
        "정도", "이상", "이하", "미만", "약간", "거의", "조차", "마저"
    ));

    public KeywordAnalysisService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * @param rawData: API에서 받은 데이터 리스트 (JSON 문자열 또는 일반 텍스트)
     * @return: 2-gram 기준 빈도 상위 4개 키워드
     */
    public List<String> analyzeKeywords(List<String> rawData) {
        StringBuilder combinedText = new StringBuilder();

        for (String data : rawData) {
            if (data == null || data.trim().isEmpty()) continue;

            if (data.trim().startsWith("{")) {
                combinedText.append(extractTextFromJson(data)).append(" ");
            } else {
                combinedText.append(data).append(" ");
            }
        }

        String text = combinedText.toString().trim();
        if (text.isEmpty()) return new ArrayList<>();

        // 1️⃣ HTML 태그 제거 (네이버 블로그 title에 <b> 태그 포함되어 있음)
        String detagged = text.replaceAll("<[^>]+>", " ");

        // 2️⃣ 특수문자 제거, 공백 정리
        String cleaned = detagged.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")
                                 .replaceAll("\\s+", " ")
                                 .trim();
        String[] tokens = cleaned.split(" ");

        // 3️⃣ 불용어·짧은 단어·순수숫자 제거
        List<String> words = Arrays.stream(tokens)
                .map(String::trim)
                .filter(w -> w.length() >= 2)
                .filter(w -> !STOPWORDS.contains(w))
                .filter(w -> !w.matches("\\d+"))
                .collect(Collectors.toList());

        // 4️⃣ 2-gram 생성
        List<String> bigrams = new ArrayList<>();
        for (int i = 0; i < words.size() - 1; i++) {
            bigrams.add(words.get(i) + " " + words.get(i + 1));
        }

        // 5️⃣ 빈도 계산 → 동적 최소 기준 → 상위 4개
        // 단어 수에 따라 기준을 동적으로 조정 (너무 적으면 2, 많으면 비례 증가)
        int minFreq = 2; // 고정 (테스트용)

        Map<String, Long> freqMap = bigrams.stream()
                .collect(Collectors.groupingBy(b -> b, Collectors.counting()));

        List<String> result = freqMap.entrySet().stream()
                .filter(e -> e.getValue() >= minFreq)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 5-1️⃣ 2-gram이 4개 미만이면 1-gram으로 보완
        if (result.size() < 4) {
            Map<String, Long> wordFreq = words.stream()
                    .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

            Set<String> alreadyIn = new HashSet<>(result);
            List<String> fallback = wordFreq.entrySet().stream()
                    .filter(e -> e.getValue() >= minFreq)
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .filter(w -> alreadyIn.stream().noneMatch(r -> r.contains(w)))
                    .limit(4 - result.size())
                    .collect(Collectors.toList());

            result.addAll(fallback);
        }

        System.out.println("====> 추출 키워드 (minFreq=" + minFreq + ", 단어수=" + words.size() + "): " + result);
        return result;
    }

    /**
     * JSON 구조에서 title / description / snippet 텍스트만 추출
     */
    private String extractTextFromJson(String json) {
        StringBuilder sb = new StringBuilder();
        try {
            JsonNode root = objectMapper.readTree(json);

            // YouTube → items, Naver → items, SerpApi → organic_results
            JsonNode items = root.has("items") ? root.get("items") : root.get("organic_results");

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    // YouTube: snippet이 Object인 구조
                    if (item.has("snippet") && item.get("snippet").isObject()) {
                        JsonNode snippet = item.get("snippet");
                        if (snippet.has("title"))       sb.append(snippet.get("title").asText()).append(" ");
                        if (snippet.has("description")) sb.append(snippet.get("description").asText()).append(" ");
                    } else {
                        // Naver / SerpApi
                        if (item.has("title"))       sb.append(item.get("title").asText()).append(" ");
                        if (item.has("description")) sb.append(item.get("description").asText()).append(" ");
                        if (item.has("snippet"))     sb.append(item.get("snippet").asText()).append(" ");
                    }
                }
            }
        } catch (Exception e) {
            return json;
        }
        return sb.toString();
    }
}