package com.example.demo.dto;

public class ChatMessage {
    private String roomId;
    private String senderType; // USER or BOT
    private String content;

    public ChatMessage() {}

    public ChatMessage(String roomId, String senderType, String content) {
        this.roomId = roomId;
        this.senderType = senderType;
        this.content = content;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}