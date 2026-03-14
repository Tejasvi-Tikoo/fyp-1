package com.wildroutes.websocket;

import lombok.Data;

import java.time.Instant;

@Data
public class ChatMessage {
    private Long senderId;
    private Long receiverId;
    private Long groupId;
    private String content;
    private Instant timestamp;
    private String type; // DIRECT, GROUP, TYPING
}

