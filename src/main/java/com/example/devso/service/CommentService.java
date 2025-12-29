package com.example.devso.service;


import com.example.devso.dto.request.CommentCreateRequest;
import com.example.devso.dto.request.CommentUpdateRequest;
import com.example.devso.dto.response.CommentResponse;
import com.example.devso.entity.Comment;
import com.example.devso.entity.Post;
import com.example.devso.entity.User;
import com.example.devso.exception.CustomException;
import com.example.devso.exception.ErrorCode;
import com.example.devso.repository.CommentRepository;
import com.example.devso.repository.PostRepository;
import com.example.devso.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse create(
            Long postId,
            Long userId,
            CommentCreateRequest request
    ) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentResponse.from(saved);
    }

    public List<CommentResponse> findByPostId(Long postId) {
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findByPostIdWithUser(postId);
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(()-> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }

        comment.markDeleted();
        commentRepository.save(comment);
    }

    @Transactional
    public CommentResponse update(Long postId, Long commentId, Long userId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getPost() == null || comment.getPost().getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        if (!comment.getPost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }

        comment.updateContent(request.getContent());
        Comment saved = commentRepository.save(comment);
        return CommentResponse.from(saved);
    }



}
