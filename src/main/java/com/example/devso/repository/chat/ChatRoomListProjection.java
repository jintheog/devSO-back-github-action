package com.example.devso.repository.chat;

import java.time.LocalDateTime;

public interface ChatRoomListProjection {
    Long getRoomId();      // AS roomId
    String getLastMessage();  // AS lastMessage
    LocalDateTime getLastMessageTime(); // AS lastMessageTime
    Integer getUnreadCount(); // AS unreadCount
    Long getOpponentId();     // AS opponentId
    String getOpponentUsername(); // AS opponentUsername
    String getOpponentName(); // AS opponentName
    String getOpponentProfileImageUrl(); // AS opponentProfileImageUrl
}