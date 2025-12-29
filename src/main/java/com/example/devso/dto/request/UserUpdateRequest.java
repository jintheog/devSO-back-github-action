package com.example.devso.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String name;
    private String bio;
    private String profileImageUrl;
    private String portfolio;
    private String phone;
    private String email;
}
