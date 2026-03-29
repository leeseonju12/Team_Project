package com.example.demo.service.apiConnect;

import com.example.demo.dto.channel.BrandSearchRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*

채널 성과 분석 페이지에서 쓰이고 있음
마인드맵 검색 결과
구글, 인스타 검색어 추출
serp 사용중

*/

@Service
@RequiredArgsConstructor
public class GoogleSearchService {

    @Value("${serpapi.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 해시태그 추출용 패턴: #한글, #영문, #숫자 조합
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([\\uAC00-\\uD7A3a-zA-Z0-9_]+)");

    // 인스타그램 모드일 때 해시태그 폴백 기준 (이 수 미만이면 일반 텍스트로 대체)
    private static final int MIN_HASHTAG_COUNT = 5;

    public List<String> getGoogleData(BrandSearchRequestDto request, boolean isInstagram) {
        List<String> snippetTexts = new ArrayList<>(); // 원본 스니펫 (폴백용)

        // 1. 검색어 설정
        String query = isInstagram ? "site:instagram.com " + request.getBrandName() : request.getBrandName();

        // 2. SerpApi URL 생성
        String apiURL = UriComponentsBuilder.fromUriString("https://serpapi.com/search")
                .queryParam("engine", "google")
                .queryParam("q", query)
                .queryParam("api_key", apiKey)
                .queryParam("num", 50)
                .queryParam("google_domain", "google.co.kr")
                .queryParam("hl", "ko")
                .build().toUriString();

        try {
            // 3. API 호출
            Map<String, Object> response = restTemplate.getForObject(apiURL, Map.class);

            if (response != null && response.get("organic_results") != null) {
                List<Object> results = (List<Object>) response.get("organic_results");

                for (Object obj : results) {
                    Map<String, Object> result = (Map<String, Object>) obj;
                    if (result.get("snippet") != null) {
                        snippetTexts.add(result.get("snippet").toString());
                    }
                }
                System.out.println("====> [" + (isInstagram ? "인스타" : "구글") + "] 스니펫 수집 완료: " + snippetTexts.size() + "건");
            } else {
                System.out.println("====> [" + (isInstagram ? "인스타" : "구글") + "] organic_results 항목 없음");
            }
        } catch (Exception e) {
            System.err.println("====> API 호출 에러: " + e.getMessage());
            e.printStackTrace();
        }

        // 4. 인스타그램 모드: 해시태그 우선 추출, 부족하면 일반 텍스트로 폴백
        if (isInstagram) {
            return extractInstagramKeywords(snippetTexts);
        }

        return snippetTexts;
    }

    /**
     * 인스타그램 스니펫에서 해시태그를 추출합니다.
     * 해시태그가 MIN_HASHTAG_COUNT개 미만이면 title 텍스트에서 단어 추출로 보완합니다.
     * 그래도 부족하면 일반 스니펫 텍스트로 폴백합니다.
     */
    private List<String> extractInstagramKeywords(List<String> snippetTexts) {
        List<String> hashtags = new ArrayList<>();

        for (String snippet : snippetTexts) {
            Matcher m = HASHTAG_PATTERN.matcher(snippet);
            while (m.find()) {
                hashtags.add(m.group(1));
            }
        }

        System.out.println("====> [인스타] 해시태그 추출: " + hashtags.size() + "개");

        if (hashtags.size() >= MIN_HASHTAG_COUNT) {
            return hashtags;
        }

        // ✅ 해시태그 부족 → 스니펫 텍스트에서 한글 단어 추출로 보완
        // 인스타 스니펫은 짧은 소개글이라 단어 자체가 키워드 역할
        List<String> wordFallback = new ArrayList<>(hashtags);
        for (String snippet : snippetTexts) {
            // #태그 제거 후 남은 텍스트에서 한글 단어 추출
            String noTag = snippet.replaceAll("#\\S+", " ");
            String[] words = noTag.split("[\s·|,]+");
            for (String w : words) {
                w = w.trim().replaceAll("[^가-힣]", "");
                if (w.length() >= 2) wordFallback.add(w);
            }
        }

        System.out.println("====> [인스타] 단어 보완 후: " + wordFallback.size() + "개");

        if (!wordFallback.isEmpty()) {
            return wordFallback;
        }

        // 최종 폴백 → 일반 스니펫 텍스트
        System.out.println("====> [인스타] 일반 텍스트로 최종 폴백");
        return snippetTexts;
    }
}