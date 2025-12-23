package com.example.devso.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {

    private Long roomId;

    @Size(max = 255, message = "메세지는 255자를 초과할 수 없습니다.")
    private String message;

}
