package com.example.devso.repository.chat;

import com.example.devso.entity.chat.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    @Query(value = """
        SELECT chat_room_id
        FROM chat_room_member
        WHERE user_id IN (:user1Id, :user2Id)
        GROUP BY chat_room_id
        HAVING COUNT(DISTINCT user_id) = 2
        LIMIT 1
        """, nativeQuery = true)
    Optional<Long> findRoomIdByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM chat_room_member WHERE chat_room_id = :chatRoomId AND user_id = :userId)", nativeQuery = true)
    long existsByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}
