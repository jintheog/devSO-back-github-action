package com.example.devso.entity.chat;

import com.example.devso.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(indexes = @Index(name = "idx_room_user", columnList = "chat_room_id, user_id"))
@Getter
@NoArgsConstructor
public class ChatRoomMember extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    private Long userId; // 실제 User 엔티티의 ID (연관관계 끊고 ID만 관리)

    @Builder
    public ChatRoomMember(ChatRoom chatRoom, Long userId) {
        this.chatRoom = chatRoom;
        this.userId = userId;
    }
}