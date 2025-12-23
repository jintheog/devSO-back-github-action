package com.example.devso.controller.chat;

import com.example.devso.Security.CustomUserDetails;
import com.example.devso.dto.response.ApiResponse;
import com.example.devso.dto.response.chat.ChatMessageResponse;
import com.example.devso.repository.chat.ChatRoomRepository;
import com.example.devso.service.chat.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Chat", description = "채팅 API")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;

    @Operation(summary = "내 채팅방 목록 조회")
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<?>> getMyRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(chatRoomRepository.findAllChatRoomsByUserId(userDetails.getId())));
    }

    @Operation(summary = "채팅방 생성 또는 입장")
    @PostMapping("/rooms/{opponentId}")
    public ResponseEntity<?> enterRoom(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long opponentId) {
        Long roomId = chatService.createOrGetRoom(userDetails.getId(), opponentId);
        return ResponseEntity.ok(Map.of("roomId", roomId));
    }

    @Operation(summary = "채팅방 메시지 내역 조회")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
        Page<ChatMessageResponse> messages = chatService.getMessages(roomId, userDetails.getId(), pageable);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "메시지 읽음 처리")
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        chatService.markAsRead(roomId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
