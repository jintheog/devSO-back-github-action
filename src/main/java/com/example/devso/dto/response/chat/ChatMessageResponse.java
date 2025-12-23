package com.example.devso.dto.response.chat;

import com.example.devso.entity.chat.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {

    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String message;
    private LocalDateTime createdAt;

    public static ChatMessageResponse of(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .messageId(msg.getId())
                .roomId(msg.getChatRoom().getId())
                .senderId(msg.getSenderId())
                .message(msg.getMessage())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
