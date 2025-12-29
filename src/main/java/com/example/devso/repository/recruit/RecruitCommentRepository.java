package com.example.devso.repository.recruit;

import com.example.devso.entity.Comment;
import com.example.devso.entity.recruit.RecruitComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecruitCommentRepository extends JpaRepository<RecruitComment, Long> {
    // 특정 게시물의 댓글 목록
    // join fetch를 통해 작성자(User)와 부모 댓글 정보를 한 번에 가져옵니다.
    @Query("select c from RecruitComment c " +
            "join fetch c.user " +
            "left join fetch c.parent " +
            "where c.recruit.id = :recruitId " +
            "order by c.createdAt asc")
    List<RecruitComment> findByRecruitIdWithUser(@Param("recruitId") Long recruitId);

    // 게시물의 댓글 수
    long countByRecruitId(Long recruitId);
}
