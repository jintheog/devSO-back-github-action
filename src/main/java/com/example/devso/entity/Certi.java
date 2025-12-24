package com.example.devso.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certis")
@Getter
@NoArgsConstructor
public class Certi extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String certiName;
    private String issuer;      // 발행처
    private String acquisitionDate; // 취득일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @Builder
    public Certi(String certiName, String issuer, String acquisitionDate, User user) {
        this.certiName = certiName;
        this.issuer = issuer;
        this.acquisitionDate = acquisitionDate;
        this.user = user;
    }


    public void setUser(User user) {
        this.user = user;
    }
}