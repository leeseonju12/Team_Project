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
    	
    	// 커스텀 속성 하드코딩 (추후 request DTO에서 받아오도록 변경 가능)
    	String industry = "베이커리 카페"; // 업종
    	String userRequirement = "이번 주말 비가 온다고 하니, 비 오는 날 어울리는 감성적인 분위기를 강조해 주세요. 그리고 주말 방문 고객에게는 아메리카노 1잔 무료 쿠폰을 증정한다는 내용을 꼭 포함해 주세요."; // 사용자 추가 요구사항

    	String prompt = String.format(

    	    "Role: %s Marketer. Task: Promo post. Lang: Korean.\n" +
    	    "Menu/Item: %s\nPlatforms: %s\nExtra: %s\nKeywords: %s\nTone: %s\nEmoji: %s\nMaxLen: %d chars.\n" +
    	    "User Requirement: %s\n" +
    	    "Rule: STRICT JSON Array ONLY. NO markdown. " +
    	    "Create one JSON object for EACH platform listed in 'Platforms'. " +
    	    "Format: [{ \"platform\": \"<PLATFORM_NAME>\", \"content\": \"...\", \"hashtags\": [\"#tag1\", \"#tag2\"] }]",
    	    
    	    // 포맷팅 변수 매핑
    	    industry,               // %s (Role에 삽입되어 업종별 마케터 역할을 부여)
    	    request.getMenuName(),  // %s
    	    request.getPlatforms(), // %s
    	    request.getExtraInfo(), // %s
    	    request.getKeywords(),  // %s
    	    request.getTones(),     // %s
    	    request.getEmojiLevel(),// %s
    	    request.getMaxLength()
    	    ,userRequirement         // %s (사용자 커스텀 요구사항 삽입)
    	);
    	
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
    
    /*
    @Value("${ai.api-key:}")
    private String geminiApiKey;

    public List<SnsResult> generateAllSnsContent(ContentRequest request) {
        // 실제 API 호출 로직이 들어갈 자리입니다.
        // String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;
        // String prompt = ... 

        // 💡 [UI 테스트용] 프론트엔드에서 넘어온 선택된 플랫폼(platforms) 텍스트를 기반으로 데이터를 굽습니다.
        return createMockResults(request); 
    }

    // 선택한 SNS에 맞춰 동적으로 결과를 만들어주는 임시 메서드
    private List<SnsResult> createMockResults(ContentRequest request) {
        List<SnsResult> results = new ArrayList<>();
        
        // request.getPlatforms()에는 "instagram,naver" 같이 콤마로 구분된 문자열이 들어옵니다.
        String requestedPlatforms = request.getPlatforms(); 
        if (requestedPlatforms == null || requestedPlatforms.isEmpty()) {
            return results;
        }

        String menuName = request.getMenuName() != null ? request.getMenuName() : "신메뉴";
        String extraInfo = request.getExtraInfo() != null ? request.getExtraInfo() : "정말 맛있어요!";

        // 1. 인스타그램이 선택된 경우
        if (requestedPlatforms.contains("instagram")) {
            SnsResult ig = new SnsResult();
            ig.setPlatform("instagram");
            ig.setPlatformAbbr("ig");
            ig.setPlatformName("Instagram");
            ig.setColor("#E1306C");
            ig.setGuideText("감성적인 문구와 해시태그를 적극적으로 활용하세요.");
            ig.setBestTime("저녁 7~9시");
            ig.setContent("✨ 매일 기다려지는 달콤한 시간!\n\n오늘 준비한 [" + menuName + "] 어떠신가요?\n" 
                        + extraInfo + "\n\n보기만 해도 기분 좋아지는 비주얼, 지금 바로 매장에서 만나보세요 💛");
            // ⭐️ 인스타그램용 해시태그 리스트 삽입
            ig.setHashtags(Arrays.asList("#" + menuName.replaceAll(" ", ""), "#소셜다모아", "#디저트맛집", "#감성카페", "#핫플추천"));
            results.add(ig);
        }

        // 2. 페이스북이 선택된 경우
        if (requestedPlatforms.contains("facebook")) {
            SnsResult fb = new SnsResult();
            fb.setPlatform("facebook");
            fb.setPlatformAbbr("fb");
            fb.setPlatformName("Facebook");
            fb.setColor("#1877F2");
            fb.setGuideText("정보 전달 위주의 깔끔하고 명확한 구성이 좋습니다.");
            fb.setBestTime("점심 12~1시");
            fb.setContent("📣 신메뉴 [" + menuName + "] 출시 안내!\n\n" 
                        + extraInfo + "\n\n친구 태그하고 같이 먹으러 갈 사람 구해보세요! 👇");
            // 페이스북은 보통 해시태그를 많이 쓰지 않으므로 생략
            results.add(fb);
        }

        // 3. 네이버 블로그가 선택된 경우
        if (requestedPlatforms.contains("naver")) {
            SnsResult nv = new SnsResult();
            nv.setPlatform("naver");
            nv.setPlatformAbbr("nv");
            nv.setPlatformName("네이버 블로그");
            nv.setColor("#03C75A");
            nv.setGuideText("검색 유입을 위해 핵심 키워드를 본문에 자연스럽게 배치하세요.");
            nv.setBestTime("오전 8~10시");
            nv.setContent("안녕하세요! 오늘은 많은 분들이 찾아주시는 [" + menuName + "]에 대해 자세히 리뷰해볼게요.\n\n" 
                        + "특히 이번에는 " + extraInfo + "라는 점이 가장 큰 매력 포인트랍니다. "
                        + "직접 오셔서 맛과 분위기를 모두 즐겨보시길 강력 추천드립니다.");
            // ⭐️ 네이버용 검색 키워드(해시태그) 삽입
            nv.setHashtags(Arrays.asList("맛집추천", menuName.replaceAll(" ", ""), "방문후기", "데이트코스"));
            results.add(nv);
        }

        // 4. 카카오 채널이 선택된 경우
        if (requestedPlatforms.contains("kakao")) {
            SnsResult kk = new SnsResult();
            kk.setPlatform("kakao");
            kk.setPlatformAbbr("kk");
            kk.setPlatformName("카카오 채널");
            kk.setColor("#F9C000");
            kk.setGuideText("모바일에서 읽기 편하도록 짧은 문장과 혜택을 강조하세요.");
            kk.setBestTime("오후 5~7시");
            kk.setContent("💛 카카오 플러스 친구 전용 소식 💛\n\n[" + menuName + "] 출시!\n" 
                        + extraInfo + "\n\n놓치지 말고 지금 바로 하단 버튼을 클릭해 혜택을 확인하세요!");
            results.add(kk);
        }

        return results;
    }
    */
    }
}