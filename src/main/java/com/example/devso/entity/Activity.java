package com.example.devso.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activities")
@Getter
@NoArgsConstructor
public class Activity extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;    // 활동 구분
    private String projectName;
    private String duration;    // 기간
    @Column(columnDefinition = "TEXT")
    private String content;     // 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Activity(String category, String projectName, String duration, String content, User user) {
        this.category = category;
        this.projectName = projectName;
        this.duration = duration;
        this.content = content;
        this.user = user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
