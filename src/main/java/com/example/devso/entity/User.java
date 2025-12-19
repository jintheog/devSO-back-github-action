package com.example.devso.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

//    @Column(nullable = false)
    private Role role;


    @Column( length = 30)
    private String phone;

    @Column(length = 500)
    private String bio;


    private String email;

    private String profileImageUrl;

    private String portfolio;

    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;


    @Builder
    public User(Long id, String username, String password, String name, Role role, String phone, String bio,
                String email, String profileImageUrl, String portfolio, AuthProvider provider, String providerId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.bio = bio;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.portfolio = portfolio;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
    }



    public void updateProfile(String name, String bio, String profileImageUrl, String portfolio) {
        if (name != null) this.name = name;
        if (bio != null) this.bio = bio;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (portfolio != null) this.portfolio = portfolio;
    }

    public void updateOauthProfile(String name, String profileImageUrl) {
        if (name != null) this.name = name;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public void updatePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }


}
