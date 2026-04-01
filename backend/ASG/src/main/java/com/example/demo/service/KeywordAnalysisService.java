package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/*
채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 검색어 추출 결과 가공중
*/

@Service
public class KeywordAnalysisService {

    private final ObjectMapper objectMapper;

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        // 육하원칙
        "누가", "누구", "언제", "어디", "어디서", "어디에", "무엇", "무슨", "왜", "어떻게", "어떤", "에서",
        // 인사·감탄
        "반갑습니다", "안녕하세요", "감사합니다", "감사해요", "고맙습니다", "고마워요",
        "안녕", "반가워", "반가워요", "환영합니다",
        // 동사형 (~기)
        "먹기", "가기", "보기", "찾기", "알기", "쓰기", "받기", "주기", "하기", "되기",
        "사기", "팔기", "열기", "닫기", "오기", "서기",
        // 동사 활용형
        "있는", "있어", "있습니다", "없는", "없어", "없습니다", "있었", "없었",
        "하는", "하고", "하지", "하면", "해서", "했습니다", "합니다", "했어요", "해요", "할게요",
        "이라", "이고", "이며", "이지", "이란", "이에요", "입니다", "이었", "사지", "가면",
        "가서", "가고", "가도", "사면", "사서", "사고", "사도",
        "보면", "보고", "오면", "오고", "되면", "되고", "오지", "보지", "되지", "있느",
        // 접속·부사
        "때문", "그리고", "그래서", "그런데", "하지만", "또한", "그냥", "바로", "물론",
        "역시", "아마", "혹시", "만약", "비록", "드디어", "갑자기", "결국",
        // 시간
        "이번", "지난", "오늘", "어제", "내일", "최근", "앞으로", "지금", "아직",
        "항상", "매일", "자주", "가끔", "요즘", "요새",
        // 정도·수량
        "정말", "너무", "많이", "조금", "아주", "매우", "더욱", "굉장히", "엄청", "완전",
        "모두", "모든", "각각", "여러", "다양한", "가장", "더", "제일",
        // 평가형
        "좋은", "좋아", "좋습니다", "좋아요", "나쁜", "싫은",
        "맛있는", "맛있어", "맛있습니다", "맛있어요",
        "예쁜", "귀여운", "멋진", "화려한",
        // 일반 동작 동사
        "가능", "필요", "관련", "경우", "통해", "위해", "대한", "따른",
        "진행", "운영", "제공", "확인", "방문", "이용", "사용", "선택",
        "소개", "공유", "업로드", "다운로드", "클릭", "검색",
        // 관계사·지시사
        "다른", "이런", "저런", "그런", "이것", "저것", "그것", "여기", "저기", "거기",
        // HTML·URL 잔재
        "https", "http", "www", "com", "co", "kr", "html", "php", "strong", "nbsp", "amp",
        // 기타 형식어
        "등등", "등을", "것이", "것을", "것은", "것도", "수도", "수는", "수를",
        "정도", "이상", "이하", "미만", "약간", "거의", "조차", "마저",
        // 추가 불용어 (소상공인 콘텐츠에서 자주 등장하는 의미없는 단어)
        "있도록", "마을", "공동체", "장소", "지역", "서울", "대한민국",
        "스튜디오", "센터", "클래스", "프로그램", "서비스", "브랜드",
        "예약", "문의", "가격", "비용", "할인", "이벤트", "신청",
        "오전", "오후", "주말", "평일", "영업", "휴무", "시간",
        "주소", "위치", "전화", "카카오", "인스타", "블로그", "홈페이지"
    ));

    public KeywordAnalysisService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * @param rawData   : API에서 받은 데이터 리스트
     * @param brandName : 브랜드명 (동적 불용어 등록용)
     * @return: 2-gram 기준 빈도 상위 4개 키워드
     */
    public List<String> analyzeKeywords(List<String> rawData, String brandName) {
        // ✅ 브랜드명 변형 생성 및 동적 불용어 등록
        Set<String> stopwords = new HashSet<>(STOPWORDS);
        List<String> brandVariants = new ArrayList<>(); // 매칭에 사용할 브랜드명 변형들

        if (brandName != null && !brandName.isBlank()) {
            String brand = brandName.trim();

            // 원본 그대로
            brandVariants.add(brand);
            stopwords.add(brand);

            // 공백 제거 버전: "그라운드 요가" → "그라운드요가"
            String noSpace = brand.replaceAll("\\s+", "");
            if (!noSpace.equals(brand)) {
                brandVariants.add(noSpace);
                stopwords.add(noSpace);
            }

            // 공백 삽입 버전: "그라운드요가" → 2~3글자 단위로 쪼개서 변형 생성
            // 한글 자모 경계 기준으로 2글자 단위 분리 시도
            if (!brand.contains(" ")) {
                for (int i = 2; i <= brand.length() - 2; i++) {
                    String variant = brand.substring(0, i) + " " + brand.substring(i);
                    brandVariants.add(variant);
                }
            }

            // 구성 단어들 (공백 기준 분리)
            Arrays.stream(brand.split("\\s+")).forEach(part -> {
                if (part.length() >= 2) {
                    brandVariants.add(part);
                    stopwords.add(part);
                }
            });
        }

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

        // 1️⃣ HTML 태그 제거
        String detagged = text.replaceAll("<[^>]+>", " ");

        // 2️⃣ 브랜드명(또는 변형) 포함 문장만 필터링
        String filteredText = detagged;
        if (!brandVariants.isEmpty()) {
            String[] sentences = detagged.split("[.!?\n]");
            StringBuilder filtered = new StringBuilder();
            for (String sentence : sentences) {
                // 브랜드명 변형 중 하나라도 포함되면 사용
                boolean relevant = brandVariants.stream().anyMatch(sentence::contains);
                if (relevant) {
                    filtered.append(sentence).append(" ");
                }
            }
            // 필터된 텍스트가 충분하면 사용, 부족하면 전체 사용
            if (filtered.toString().trim().split("\\s+").length >= 10) {
                filteredText = filtered.toString();
            }
        }

        // 3️⃣ 특수문자 제거, 공백 정리
        String cleaned = filteredText.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")
                                 .replaceAll("\\s+", " ")
                                 .trim();
        String[] tokens = cleaned.split(" ");

        // 3️⃣ 불용어·짧은 단어·순수숫자·조사 붙은 단어 제거
        // ✅ 한글은 2자 이상, 영어는 4자 이상 (ng, ch 같은 영어 조각 제거)
        List<String> words = Arrays.stream(tokens)
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .filter(w -> w.matches("[가-힣]+") ? w.length() >= 2 : w.length() >= 4)
                .filter(w -> !stopwords.contains(w))
                .filter(w -> !w.matches("\\d+"))
                // ✅ 한국어 조사/어미로 끝나는 단어 제거 (몸을→제거, 머리를→제거)
                .filter(w -> !w.matches("[가-힣]+(을|를|이|가|은|는|의|에|로|으로|와|과|도|만|고|며|서|게|께|한테|에서|까지|부터|보다|라고|이라고|하고|처럼|만큼|마다|조차|라도|이라도|든지|이든지)"))
                .collect(Collectors.toList());

        // 4️⃣ 1-gram 빈도 계산 (한글 단어만)
        Map<String, Long> wordFreq = words.stream()
                .filter(w -> w.matches("[가-힣]+"))
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        int minFreq = words.size() < 50 ? 1 : 2;

        // 5️⃣ 빈도순 상위 4개 추출
        List<String> result = wordFreq.entrySet().stream()
                .filter(e -> e.getValue() >= minFreq)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 5-1️⃣ 부족하면 minFreq 무시하고 빈도순 top 채우기
        if (result.size() < 4) {
            Set<String> alreadyIn = new HashSet<>(result);
            List<String> lastResort = wordFreq.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .filter(w -> !alreadyIn.contains(w))
                    .limit(4 - result.size())
                    .collect(Collectors.toList());
            result.addAll(lastResort);
        }

        System.out.println("====> 추출 키워드 (minFreq=" + minFreq + ", 단어수=" + words.size() + "): " + result);
        return result;
    }

    /**
     * 하위 호환용 - brandName 없이 호출 시
     */
    public List<String> analyzeKeywords(List<String> rawData) {
        return analyzeKeywords(rawData, null);
    }

    /**
     * JSON 구조에서 title / description / snippet 텍스트만 추출
     */
    private String extractTextFromJson(String json) {
        StringBuilder sb = new StringBuilder();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.has("items") ? root.get("items") : root.get("organic_results");

            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    if (item.has("snippet") && item.get("snippet").isObject()) {
                        JsonNode snippet = item.get("snippet");
                        if (snippet.has("title"))       sb.append(snippet.get("title").asText()).append(" ");
                        if (snippet.has("description")) sb.append(snippet.get("description").asText()).append(" ");
                    } else {
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