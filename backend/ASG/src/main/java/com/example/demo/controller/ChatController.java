package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.demo.dto.ChatMessage;
import com.example.demo.service.ChatbotService;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatbotService chatbotService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatbotService.saveMessage(chatMessage);
        
        ChatMessage botResponse = chatbotService.generateBotResponse(chatMessage);
        
        chatbotService.saveMessage(botResponse);
        
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), botResponse);
    }
}