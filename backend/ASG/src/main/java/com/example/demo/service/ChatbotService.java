package com.example.demo.service;

import com.example.demo.dto.ChatMessage;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Collections;

@Service
public class ChatbotService {

    public void saveMessage(ChatMessage message) {
        // Todo: JPA 등을 활용한 DB 저장 로직 구현
        System.out.println("Message saved [" + message.getSenderType() + "]: " + message.getContent());
    }

    public ChatMessage generateBotResponse(ChatMessage userMessage) {
        String userInput = userMessage.getContent().trim();
        String roomId = userMessage.getRoomId();
        
        ChatMessage botResponse = new ChatMessage();
        botResponse.setRoomId(roomId);
        botResponse.setSenderType("BOT");

            
            // 1. Exact Match: 하위 메뉴 및 특정 액션 버튼 (가장 높은 우선순위)
            if (userInput.equals("연동 가능 SNS 확인")) {
                botResponse.setContent("현재 인스타그램(Business), 페이스북 페이지, 네이버 블로그, X(트위터) 연동을 지원합니다. 추가 채널은 업데이트 예정입니다.");
                botResponse.setQuickReplies(Arrays.asList("인스타그램 연동 가이드", "블로그 연동 가이드", "메뉴로 돌아가기"));
            } else if (userInput.equals("연동 오류 해결")) {
                botResponse.setContent("연동 오류는 주로 API 토큰 만료로 발생합니다. [설정 > 계정 관리]에서 '연동 갱신' 버튼을 눌러주세요.");
                botResponse.setQuickReplies(Arrays.asList("상담원 연결", "메뉴로 돌아가기"));
            } else if (userInput.equals("AI 초안 만들기")) {
                botResponse.setContent("홍보하고 싶은 상품의 키워드나 사진을 업로드하시면 AI가 3가지 버전의 마케팅 문구를 생성합니다.");
                botResponse.setQuickReplies(Arrays.asList("이미지 업로드", "텍스트로 생성", "메뉴로 돌아가기"));
            } else if (userInput.equals("포스팅 예약 방법")) {
                botResponse.setContent("콘텐츠 제작 후 '예약 게시' 버튼을 클릭하여 원하는 날짜와 시간을 선택하면 여러 SNS에 동시 발행됩니다.");
                botResponse.setQuickReplies(Arrays.asList("예약 현황 확인", "메뉴로 돌아가기"));
            } else if (userInput.equals("자동 답글 설정")) {
                botResponse.setContent("특정 키워드가 포함된 리뷰나 댓글에 AI가 자동으로 답글을 달도록 설정하여 운영 공수를 줄일 수 있습니다.");
                botResponse.setQuickReplies(Arrays.asList("키워드 설정하기", "메뉴로 돌아가기"));
            }

            // 2. Keyword Match: 상위 카테고리 및 일반 질의 (중간 우선순위)
            else if (userInput.equals("시작") || userInput.equals("메뉴") || userInput.equals("처음으로")) {
                botResponse.setContent("소셜다모아 고객센터입니다. 원하시는 서비스 카테고리를 선택해 주세요.");
                botResponse.setQuickReplies(Arrays.asList("SNS 연동 관리", "AI 콘텐츠 자동생성", "리뷰/댓글 관리", "요금제 안내"));
            } else if (userInput.contains("연동") || userInput.contains("계정") || userInput.contains("연결")) {
                botResponse.setContent("SNS 계정 연동 및 관리 관련 메뉴입니다.");
                botResponse.setQuickReplies(Arrays.asList("연동 가능 SNS 확인", "연동 오류 해결", "메뉴로 돌아가기"));
            } else if (userInput.contains("자동") || userInput.contains("생성") || userInput.contains("콘텐츠") || userInput.contains("포스팅")) {
                botResponse.setContent("AI를 활용한 콘텐츠 자동 생성 및 예약 포스팅 메뉴입니다.");
                botResponse.setQuickReplies(Arrays.asList("AI 초안 만들기", "포스팅 예약 방법", "메뉴로 돌아가기"));
            } else if (userInput.contains("리뷰") || userInput.contains("댓글") || userInput.contains("답글")) {
                botResponse.setContent("통합 리뷰 관리 및 AI 자동 답글 관련 메뉴입니다.");
                botResponse.setQuickReplies(Arrays.asList("미확인 댓글 보기", "자동 답글 설정", "메뉴로 돌아가기"));
            } else if (userInput.contains("요금") || userInput.contains("가격") || userInput.contains("유료")) {
                botResponse.setContent("소셜다모아는 비즈니스 규모에 맞는 다양한 요금제를 제공합니다.");
                botResponse.setQuickReplies(Arrays.asList("요금제 상세 보기", "무료 체험 신청", "메뉴로 돌아가기"));
            }

            // 3. Default: 매칭 실패 시 처리 (가장 낮은 우선순위)
            else {
                botResponse.setContent("입력하신 내용을 이해하지 못했습니다. 아래 버튼을 이용하시거나 상담원에게 직접 문의해 주세요.");
                botResponse.setQuickReplies(Arrays.asList("메뉴로 돌아가기", "상담원 연결"));
            }

            return botResponse;
        }
}