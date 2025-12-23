package com.example.devso.service;

import com.example.devso.dto.request.PasswordChangeRequest;
import com.example.devso.dto.request.UserUpdateRequest;
import com.example.devso.dto.response.UserProfileResponse;
import com.example.devso.dto.response.UserResponse;
import com.example.devso.entity.User;
import com.example.devso.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        return UserProfileResponse.of(user);
    }


    @Transactional
    public UserProfileResponse updateProfile(String username, Long currentUserId, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        if (!user.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("사용자가 아닙니다.");
        }

        user.updateProfile(
                request.getName(),
                request.getBio(),
                request.getProfileImageUrl(),
                request.getPortfolio()

        );

        return UserProfileResponse.of(user);
    }


    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("새로운 비밀번호를 입력해야 합니다.");
        }

        String newEncodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(newEncodedPassword);
    }

    public List<UserResponse> searchUsers(String query, Long excludeUserId) {
        List<User> users = userRepository.searchUsers(query, excludeUserId);
        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
}