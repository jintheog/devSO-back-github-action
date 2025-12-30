package com.example.devso.entity.recruit;

import com.example.devso.dto.request.recruit.RecruitRequest;
import com.example.devso.entity.BaseEntity;
import com.example.devso.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recruits")
@SQLDelete(sql = "UPDATE recruits SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL") // 조회 시 삭제된 데이터 제외
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 모집 구분 (스터디 / 프로젝트)
    @Column(nullable = false)
    private RecruitType type;

    @Column(nullable = false)
    private String title;

    @Lob // 대용량 데이터를 저장할 때 사용
    @Column(nullable = false, columnDefinition = "TEXT") // DB에 TEXT 타입으로 생성되도록 지정
    private String content;

    // 모집 인원
    @Column(nullable = false)
    private int totalCount;

    @Column(nullable = false)
    private int currentCount = 0;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 모집 상태
    @Column(nullable = false)
    private RecruitStatus status = RecruitStatus.OPEN;

    // 모집 포지션 (다중 선택)
    @BatchSize(size = 10)
    @ElementCollection(targetClass = RecruitPosition.class)
    @CollectionTable(name = "recruit_positions", joinColumns = @JoinColumn(name = "recruit_id"))
    @Column(name = "position")
    private List<RecruitPosition> positions = new ArrayList<>();

    // 진행 방식
    private RecruitProgressType progressType;

    // 모집 기간
    private RecruitDuration duration;

    // 연락 방법
    private RecruitContactMethod contactMethod;

    // 연락 정보 (이메일 / 링크 / 전화번호 등)
    @Column(length = 255)
    private String contactInfo;

    // 모집 기술 스택 (다중 선택)
    @BatchSize(size = 10)
    @ElementCollection(targetClass = TechStack.class)
    @CollectionTable(name = "recruit_stacks", joinColumns = @JoinColumn(name = "recruit_id"))
    @Column(name = "stack")
    private List<TechStack> stacks = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate deadLine;

    @Column(nullable = false)
    private long viewCount = 0;

    @Column(nullable = false)
    private long commentCount = 0;

    // 팀원 모집 게시글 삭제 시 댓글들도 삭제(deleted_at에 삭제일 추가)
    @OneToMany(mappedBy = "recruit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecruitComment> recruitComments = new ArrayList<>();

    // 북마크 역시 게시글 삭제 시 함께 삭제(deleted_at에 삭제일 추가)
    @OneToMany(mappedBy = "recruit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecruitBookMark> recruitBookMarks = new ArrayList<>();

    // ===== 생성 =====
    public static Recruit create(User user, RecruitRequest req) {
        Recruit recruit = new Recruit();
        recruit.user = user;
        recruit.title = req.getTitle();
        recruit.content = req.getContent();
        recruit.type = req.getType();
        recruit.positions = req.getPositions();
        recruit.progressType = req.getProgressType();
        recruit.duration = req.getDuration();
        recruit.contactMethod = req.getContactMethod();
        recruit.contactInfo = req.getContactInfo();
        recruit.stacks = req.getStacks();
        recruit.totalCount = req.getTotalCount();
        recruit.status = RecruitStatus.OPEN;
        recruit.deadLine = req.getDeadLine();
        recruit.viewCount = 0;
        recruit.currentCount = 0;
        recruit.commentCount = 0; // 초기값 명시
        return recruit;
    }

    // ===== 수정 =====
    public void update(
            String title,
            String content,
            List<RecruitPosition> positions,
            RecruitProgressType progressType,
            RecruitDuration duration,
            RecruitContactMethod contactMethod,
            String contactInfo,
            List<TechStack> stacks,
            int totalCount,
            LocalDate deadLine
    ) {
        this.title = title;
        this.content = content;
        this.positions = positions;
        this.progressType = progressType;
        this.duration = duration;
        this.contactMethod = contactMethod;
        this.contactInfo = contactInfo;
        this.stacks = stacks;
        this.totalCount = totalCount;
        this.deadLine = deadLine;
    }

    // ===== 조회수 증가 =====
    public void increaseViewCount() {
        this.viewCount++;
    }

    // ===== 모집 인원 증가 =====
    public void increaseCurrentCount() {
        if (currentCount >= totalCount) {
            throw new IllegalStateException("모집 인원 초과");
        }
        currentCount++;
    }

    //===== 댓글 수 증가 =====
    public void increaseCommentCount() {
        this.commentCount++;
    }

    // ===== 댓글 수 감소(부모 댓글 삭제 시 하위 자식 댓글들 모두 삭제) =====
    public void decreaseCommentCountBy(long amount) {
        if (this.commentCount >= amount) {
            this.commentCount -= amount;
        } else {
            this.commentCount = 0; // 마이너스 방지
        }
    }

    // ===== 작성자 검증 =====
    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    // ===== 모집 마감 =====
    public void close() {
        this.status = RecruitStatus.CLOSED;
    }

    // ===== 모집 재오픈 =====
    public void open() {
        this.status = RecruitStatus.OPEN;
    }
}