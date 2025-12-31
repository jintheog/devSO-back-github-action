package com.example.devso.entity.recruit;

import com.example.devso.entity.BaseEntity;
import com.example.devso.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(
        name = "recruit_ai_checklists",
        uniqueConstraints = {
                // 동일 유저가 동일 모집글에 대해 중복 데이터를 생성하지 않도록 방지
                @UniqueConstraint(columnNames = {"user_id", "recruit_id"})
        }
)
@SQLDelete(sql = "UPDATE recruit_ai_checklists SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL") // 모든 조회(find, join 등) 시 삭제된 데이터 자동 제외
public class RecruitAiChecklist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id", nullable = false)
    private Recruit recruit;

    @Column(nullable = false)
    private Integer score; // AI 자가진단 점수

    @Column(columnDefinition = "TEXT", nullable = false)
    private String aiResponse; // Gemini가 생성한 JSON 데이터 저장
}