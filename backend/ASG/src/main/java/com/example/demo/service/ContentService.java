package com.example.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.example.demo.dto.ContentRequest;
import com.example.demo.dto.SnsResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

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
}