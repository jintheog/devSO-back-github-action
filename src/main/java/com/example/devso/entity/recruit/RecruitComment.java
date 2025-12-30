package com.example.devso.entity.recruit;

import com.example.devso.entity.BaseEntity;
import com.example.devso.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recruit_comments")
@SQLDelete(sql = "UPDATE recruit_comments SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@NoArgsConstructor
public class RecruitComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_id", nullable = false)
    private Recruit recruit;

    //  1. 대댓글을 위한 자기 참조 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // DB에 parent_id 컬럼이 생성
    private RecruitComment parent;

    //  2. 부모 댓글 입장에서 자식들을 조회하기 위한 리스트
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecruitComment> children = new ArrayList<>();

    // ===== 생성 =====
    public static RecruitComment create(String content, User user, Recruit recruit, RecruitComment parent) {
        RecruitComment comment = new RecruitComment();
        comment.content = content;
        comment.user = user;
        comment.recruit = recruit;
        if(parent != null){
            comment.setParent(parent);
        }
        return comment;
    }

    // ===== 수정 =====
    public void update(String content) {
        this.content = content;
    }

    private void setParent(RecruitComment parent) {
        this.parent = parent;
        parent.getChildren().add(this); // 부모의 children 리스트에도 즉시 추가
    }
}