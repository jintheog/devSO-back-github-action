package com.example.devso.dto.response.recruit;

import com.example.devso.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecruitUserResponse {

    private Long id;
    private String username;
    private String name;
    private String profileImageUrl;


    public static RecruitUserResponse from(User user) {
        return RecruitUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}