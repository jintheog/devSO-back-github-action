package com.example.devso.entity.chat;

import com.example.devso.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(indexes = {
        @Index(name = "idx_room_created", columnList = "chat_room_id, createdAt")
})
@NoArgsConstructor
@Getter
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    private Long senderId;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean isRead = false;

    @Builder
    public ChatMessage(ChatRoom chatRoom, Long senderId, String message, boolean isRead) {
        this.chatRoom = chatRoom;
        this.senderId = senderId;
        this.message = message;
        this.isRead = isRead;
    }
}
