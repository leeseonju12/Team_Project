package com.example.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.SnsResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

	private final GeminiApiClient geminiClient; // 공통 모듈 주입!
    private final ObjectMapper objectMapper = new ObjectMapper();
	
    public List<SnsResult> generateAllSnsContent(ContentRequest request) {
    	
    	String prompt = String.format(
//                "너는 카페 매니저야. 메뉴 '%s'에 대해 %s 플랫폼용 홍보 문구를 작성해줘. " +
//                "추가정보: %s, 키워드: %s, 말투: %s, 이모지 수준: %s, 글자수: %d자 이내. " +
//                "응답은 반드시 순수한 JSON 배열 형식으로만 해줘. 마크다운 기호(```json)는 절대 쓰지 마. "
				"Role: Cafe Manager. Task: Promo post. Lang: Korean.\n" +
				"Menu: %s\nPlatforms: %s\nExtra: %s\nKeywords: %s\nTone: %s\nEmoji: %s\nMaxLen: %d chars.\n" +
				"Rule: STRICT JSON Array ONLY. NO markdown, " +
                "form: [{ \"platform\": \"instagram\", \"content\": \"...\", \"hashtags\": [\"#tag1\", \"#tag2\"] }]",
                request.getMenuName(), request.getPlatforms(), request.getExtraInfo(), 
                request.getKeywords(), request.getTones(), request.getEmojiLevel(), request.getMaxLength()
            );
    	if (request.getUploadedImgUrls() != null && !request.getUploadedImgUrls().isEmpty()) {
    		prompt += "\n(참고: 사용자가 이미지를 함께 업로드했습니다. 이미지와 어울리는 톤으로 작성해주세요.)";
        }
    	
        // 2. 공통 모듈에게 질문 던지고 답변 받기 (통신 로직을 직접 안 짜도 됨!)
        String rawJsonContent = geminiClient.requestToGemini(prompt);
        
        // 3. 답변 파싱해서 돌려주기
        return parseAndEnrichResults(rawJsonContent);
    }
    
    private List<SnsResult> parseAndEnrichResults(String rawJsonContent) {
        List<SnsResult> results = new ArrayList<>();
        
        try {
            // AI가 가끔 마크다운 찌꺼기(```json)를 붙여서 주면 파싱 에러가 나므로 안전하게 제거
            String cleanJson = rawJsonContent.replace("```json", "").replace("```", "").trim();
            
            // JSON 문자열 -> List<SnsResult> 변환
            List<SnsResult> parsedList = objectMapper.readValue(cleanJson, new TypeReference<List<SnsResult>>() {});

            // 화면을 그리는 데 필요한 UI 데이터 덧붙이기
            for (SnsResult item : parsedList) {
                switch (item.getPlatform()) {
                    case "instagram":
                        item.setPlatformAbbr("ig");
                        item.setPlatformName("Instagram");
                        item.setColor("#E1306C");
                        item.setGuideText("감성적인 문구와 해시태그를 활용하세요.");
                        item.setBestTime("저녁 7~9시");
                        break;
                    case "facebook":
                        item.setPlatformAbbr("fb");
                        item.setPlatformName("Facebook");
                        item.setColor("#1877F2");
                        item.setGuideText("정보 전달 위주의 깔끔한 구성이 좋습니다.");
                        item.setBestTime("점심 12~1시");
                        break;
                    case "naver":
                        item.setPlatformAbbr("nv");
                        item.setPlatformName("네이버 블로그");
                        item.setColor("#03C75A");
                        item.setGuideText("검색 유입을 위해 키워드를 자연스럽게 배치하세요.");
                        item.setBestTime("오전 8~10시");
                        break;
                    case "kakao":
                        item.setPlatformAbbr("kk");
                        item.setPlatformName("카카오 채널");
                        item.setColor("#F9C000");
                        item.setGuideText("가독성이 좋은 짧은 문장과 명확한 혜택을 강조하세요.");
                        item.setBestTime("오후 5~7시");
                        break;
                }
                results.add(item);
            }
        } catch (Exception e) {
            System.err.println("AI JSON 파싱 중 오류 발생: " + e.getMessage());
            // 필요한 경우 여기서 기본(Fallback) 에러 메시지를 담은 SnsResult를 반환하도록 처리할 수 있습니다.
        }

        return results;

    }
}