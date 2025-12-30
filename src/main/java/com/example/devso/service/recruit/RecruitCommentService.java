package com.example.devso.service.recruit;

import com.example.devso.dto.request.recruit.RecruitCommentRequest;
import com.example.devso.dto.response.recruit.RecruitCommentResponse;
import com.example.devso.entity.User;
import com.example.devso.entity.recruit.Recruit;
import com.example.devso.entity.recruit.RecruitComment;
import com.example.devso.exception.CustomException;
import com.example.devso.exception.ErrorCode;
import com.example.devso.repository.UserRepository;
import com.example.devso.repository.recruit.RecruitCommentRepository;
import com.example.devso.repository.recruit.RecruitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitCommentService {
    private final RecruitCommentRepository recruitCommentRepository;
    private final RecruitRepository recruitRepository;
    private final UserRepository userRepository;

    // 댓글 및 대댓글 생성
    @Transactional
    public RecruitCommentResponse create(Long recruitId, Long userId, RecruitCommentRequest request) {
        Recruit recruit = recruitRepository.findById(recruitId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUIT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        RecruitComment parent = null;

        // 대댓글 로직 추가
        if (request.getParentId() != null) {
            parent = recruitCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

            // 대닷글 1개만 허용
            // 만약 답글을 단 댓글이 이미 답글(parent가 존재)이라면,
            // 그 댓글의 부모를 현재 댓글의 부모로 설정하여 Depth 1을 유지함
            if (parent.getParent() != null) {
                parent = parent.getParent();
            }
        }

        RecruitComment comment = RecruitComment.create(request.getContent(), user, recruit, parent);
        recruitCommentRepository.save(comment);

        recruit.increaseCommentCount();
        return RecruitCommentResponse.from(comment, userId);
    }

    // 특정 모집글의 댓글 조회 (계층형)
    public List<RecruitCommentResponse> findByRecruitId(Long recruitId, Long currentUserId) {
        if (!recruitRepository.existsById(recruitId)) {
            throw new CustomException(ErrorCode.RECRUIT_NOT_FOUND);
        }

        // 전체 댓글을 가져오되, Response DTO 내부의 children 처리 로직에 의해 계층 구조 형성
        List<RecruitComment> comments = recruitCommentRepository.findByRecruitIdWithUser(recruitId);

        return comments.stream()
                // 최상위 댓글(부모가 없는 댓글)만 필터링하여 반환 리스트 생성
                // 자식 댓글들은 최상위 댓글의 children 리스트 안에 자동으로 포함됨
                .filter(comment -> comment.getParent() == null)
                .map(comment -> RecruitCommentResponse.from(comment, currentUserId))
                .toList();
    }

    // 댓글 수정
    @Transactional
    public RecruitCommentResponse update(Long commentId, Long userId, RecruitCommentRequest request) {
        RecruitComment comment = recruitCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }

        comment.update(request.getContent());
        return RecruitCommentResponse.from(comment, userId);
    }

    // 댓글 삭제
    @Transactional
    public void delete(Long commentId, Long userId) {
        RecruitComment comment = recruitCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }

        // 본인 + 자식 댓글들의 수
        int totalToDelete = 1 + comment.getChildren().size();
        comment.getRecruit().decreaseCommentCountBy(totalToDelete);
        recruitCommentRepository.delete(comment);
    }
}