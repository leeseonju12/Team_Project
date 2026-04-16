package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChatMessage {
    private String roomId;
    private String senderType; // USER or BOT
    private String content;
    private List<String> quickReplies;



}