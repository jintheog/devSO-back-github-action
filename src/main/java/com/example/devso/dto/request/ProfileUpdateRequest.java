package com.example.devso.dto.request;

import com.example.devso.entity.*;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequest {

    // 1. User 기본 정보 수정 필드
    private String name;
    private String bio;
    private String profileImageUrl;
    private String portfolio;
    private String phone; // 추가됨
    private String email;

    // 2. 이력 리스트 수정 필드
    private List<CareerUpdateDto> careers;
    private List<EducationUpdateDto> educations;
    private List<CertiUpdateDto> certis;
    private List<ActivityUpdateDto> activities;
    private List<SkillUpdateDto> skills; // Skill 추가

    // --- 내부 이력 DTO 및 엔티티 변환 로직 ---

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CareerUpdateDto {
        private String companyName;
        private String department;
        private String position;
        private String startDate;
        private String endDate;
        private String task;

        public Career toEntity(User user) {
            return Career.builder()
                    .companyName(companyName)
                    .department(department)
                    .position(position)
                    .startDate(startDate)
                    .endDate(endDate)
                    .task(task)
                    .user(user)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationUpdateDto {
        private String schoolName;
        private String major;
        private String startDate;
        private String endDate;

        public Education toEntity(User user) {
            return Education.builder()
                    .schoolName(schoolName)
                    .major(major)
                    .startDate(startDate)
                    .endDate(endDate)
                    .user(user)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertiUpdateDto {
        private String certiName;
        private String issuer;
        private String acquisitionDate;

        public Certi toEntity(User user) {
            return Certi.builder()
                    .certiName(certiName)
                    .issuer(issuer)
                    .acquisitionDate(acquisitionDate)
                    .user(user)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityUpdateDto {
        private String category;
        private String projectName;
        private String duration;
        private String content;

        public Activity toEntity(User user) {
            return Activity.builder()
                    .category(category)
                    .projectName(projectName)
                    .duration(duration)
                    .content(content)
                    .user(user)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillUpdateDto {
        private String name;
        private String level;

        public Skill toEntity(User user) {
            return Skill.builder()
                    .name(name)
                    .level(level)
                    .user(user)
                    .build();
        }
    }
}