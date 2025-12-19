package com.example.devso.controller;

import com.example.devso.dto.request.KakaoLoginRequest;
import com.example.devso.dto.request.LoginRequest;
import com.example.devso.dto.request.SignupRequest;
import com.example.devso.dto.response.ApiResponse;
import com.example.devso.dto.response.TokenResponse;
import com.example.devso.dto.response.UserResponse;
import com.example.devso.Security.CustomUserDetails;
import com.example.devso.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = authService.getMe(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "카카오 로그인")
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request
    ){
        TokenResponse response = authService.kakaoLogin(request.getCode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }

}