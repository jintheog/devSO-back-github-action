package com.example.devso.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "careers")
@Getter
@NoArgsConstructor
public class Career extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String department;
    private String startDate; // 입사년월
    private String endDate;   // 퇴사년월
    private String position;  // 직급/직책
    private String task;      // 담당업무

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Career(String companyName, String department, String startDate, String endDate, String position, String task, User user) {
        this.companyName = companyName;
        this.department = department;
        this.startDate = startDate;
        this.endDate = endDate;
        this.position = position;
        this.task = task;
        this.user = user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}