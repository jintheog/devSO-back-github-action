package com.example.devso.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "skills")
@Getter
@NoArgsConstructor
public class Skill extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // 기술명 (예: React, Spring Boot)
    private String level; // 숙련도 (상, 중, 하)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Skill(String name, String level, User user) {
        this.name = name;
        this.level = level;
        this.user = user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}