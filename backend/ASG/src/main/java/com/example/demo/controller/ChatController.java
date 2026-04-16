package com.example.demo.controller;

import com.example.demo.dto.ChatMessage;
import com.example.demo.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatbotService chatbotService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage message) {
        // 1. 사용자 메시지 저장
        chatbotService.saveMessage(message);
        
        // 2. 서비스 계층에 비즈니스 로직 위임 (응답 생성 및 선택지 포함)
        ChatMessage botResponse = chatbotService.generateBotResponse(message);
        
        // 3. 봇 응답 저장
        chatbotService.saveMessage(botResponse);
        
        // 4. 클라이언트에 메시지 전송
        messagingTemplate.convertAndSend("/topic/" + message.getRoomId(), botResponse);
    }
}