package com.example.devso.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class KakaoLoginRequest {

    @NotBlank(message = "Auth code 는 필수 입니다.")
    private String code;
}
