package com.example.devso.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "educations")
@Getter
@NoArgsConstructor
public class Education extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String major;       // 학력정보
    private String schoolName;  // 교육명
    private String startDate;
    private String endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @Builder
    public Education(String major, String schoolName, String startDate, String endDate, User user) {
        this.major = major;
        this.schoolName = schoolName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
    }


    public void setUser(User user) {
        this.user = user;
    }
}