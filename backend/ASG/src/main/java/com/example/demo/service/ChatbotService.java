package com.example.demo.service;

import com.example.demo.dto.ChatMessage;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    public void saveMessage(ChatMessage message) {
        // Todo: DB에 메시지 저장 로직 구현
        System.out.println("Message saved: " + message.getContent());
    }

    public ChatMessage generateBotResponse(ChatMessage userMessage) {
        // 임시 에코(Echo) 응답 로직
        // Todo: 실제 챗봇 응답 생성 로직 구현 (키워드 매칭, 외부 API 호출 등)
        String responseContent = "챗봇 응답입니다: [" + userMessage.getContent() + "]에 대한 안내입니다.";
        
        return new ChatMessage(
                userMessage.getRoomId(),
                "BOT",
                responseContent
        );
    }
}