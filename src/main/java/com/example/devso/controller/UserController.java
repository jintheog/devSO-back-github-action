package com.example.devso.controller;

import com.example.devso.Security.CustomUserDetails;
import com.example.devso.dto.request.PasswordChangeRequest;
import com.example.devso.dto.request.UserUpdateRequest;
import com.example.devso.dto.response.ApiResponse;
import com.example.devso.dto.response.UserProfileResponse;
import com.example.devso.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;


    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }

        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
    }
    @Operation(summary = "프로필 조회")
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @Parameter(description = "조회할 사용자명")
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        if (userDetails == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다. (토큰 확인 필요)");
        }

        if (!isAdmin(userDetails) && !userDetails.getUsername().equals(username)) {
            throw new IllegalArgumentException("다른 사용자의 프로필을 조회할 권한이 없습니다.");
        }
        UserProfileResponse response = userService.getProfile(username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "프로필 수정")
    @PutMapping("/{username}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Parameter(description = "사용자명")
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        if (!isAdmin(userDetails) && !userDetails.getUsername().equals(username)) {
            throw new AccessDeniedException("다른 사용자의 프로필을 수정할 권한이 없습니다.");
        }

        UserProfileResponse response = userService.updateProfile(username, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @Operation(summary = "비밀번호 변경")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        String username = userDetails.getUsername();
        userService.changePassword(username, request);

        return ResponseEntity.noContent().build();
    }



}