package com.example.devso.repository.chat;

import com.example.devso.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query(value = """
        SELECT
            cr.id AS roomId,
            m.message AS lastMessage,
            m.created_at AS lastMessageTime,
            (SELECT COUNT(*) FROM chat_message cm
             WHERE cm.chat_room_id = cr.id
             AND cm.sender_id != :userId
             AND cm.is_read = false) AS unreadCount,
            other_m.user_id AS opponentId,
            opponent_user.username AS opponentUsername,
            opponent_user.name AS opponentName,
            opponent_user.profile_image_url AS opponentProfileImageUrl
        FROM chat_room cr
        INNER JOIN chat_room_member my_m ON cr.id = my_m.chat_room_id AND my_m.user_id = :userId
        INNER JOIN chat_room_member other_m ON cr.id = other_m.chat_room_id AND other_m.user_id != :userId
        INNER JOIN users opponent_user ON other_m.user_id = opponent_user.id
        LEFT JOIN chat_message m ON m.id = (
            SELECT id FROM chat_message
            WHERE chat_room_id = cr.id
            ORDER BY created_at DESC LIMIT 1
        )
        ORDER BY lastMessageTime DESC
        """, nativeQuery = true)
        List<ChatRoomListProjection> findAllChatRoomsByUserId(@Param("userId") Long userId);
    }
