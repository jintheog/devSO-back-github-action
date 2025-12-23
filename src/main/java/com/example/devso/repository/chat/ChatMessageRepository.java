package com.example.devso.repository.chat;

import com.example.devso.entity.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Modifying
    @Transactional
        @Query(value = """
            UPDATE chat_message
            SET is_read = true
            WHERE chat_room_id = :roomId
            AND sender_id != :userId
            AND is_read = false
            """, nativeQuery = true)
        int updateReadStatus(@Param("roomId") Long roomId, @Param("userId") Long userId);
    
        @Query(value = "SELECT * FROM chat_message WHERE chat_room_id = :chatRoomId ORDER BY created_at DESC",
               countQuery = "SELECT COUNT(*) FROM chat_message WHERE chat_room_id = :chatRoomId",
               nativeQuery = true)
        Page<ChatMessage> findByChatRoomId(@Param("chatRoomId") Long chatRoomId, Pageable pageable);
    }