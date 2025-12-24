package com.example.devso.service;

import com.example.devso.dto.request.PasswordChangeRequest;
import com.example.devso.dto.request.ProfileUpdateRequest;
import com.example.devso.dto.request.UserUpdateRequest;
import com.example.devso.dto.response.UserProfileResponse;
import com.example.devso.dto.response.UserResponse;
import com.example.devso.entity.*;
import com.example.devso.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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


    public UserProfileResponse getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 엔티티를 DTO로 변환하여 반환
        return UserProfileResponse.from(user);
    }
    /**
     * 프로필 및 이력 정보 통합 수정
     */
    @Transactional
    public void updateFullProfileByUsername(String username, ProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 1. 기본 프로필 정보 업데이트
        user.updateProfile(
                request.getBio(),
                request.getProfileImageUrl(),
                request.getPortfolio(),
                request.getName(),
                request.getPhone()
        );

        // 2. 경력 사항 업데이트 (List)
        // orphanRemoval = true 설정 덕분에 clear() 후 addAll() 하면 삭제/수정이 자동으로 일어납니다.
        if (request.getCareers() != null) {
            List<Career> newCareers = request.getCareers().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateCareers(newCareers);
        }

        // 3. 학력 사항 업데이트
        if (request.getEducations() != null) {
            List<Education> newEducations = request.getEducations().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateEducations(newEducations); // User 엔티티에 메서드 추가 필요
        }

        if (request.getActivities() != null) {
            List<Activity> newActivities = request.getActivities().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateActivities(newActivities);
        }

        if (request.getCertis() != null) {
            List<Certi> newCerti = request.getCertis().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateCertis(newCerti);
        }

        if (request.getSkills() != null) {
            List<Skill> newSkill = request.getSkills().stream()
                    .map(dto -> dto.toEntity(user))
                    .toList();
            user.updateSkills(newSkill);
        }
    }


    @Transactional
    public UserProfileResponse updateProfile(String username, Long currentUserId, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        if (!user.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("사용자가 아닙니다.");
        }

        user.updateProfile(
                request.getBio(),
                request.getProfileImageUrl(),
                request.getPortfolio(),
                request.getName(),
                request.getPhone()
        );

        return UserProfileResponse.from(user);
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