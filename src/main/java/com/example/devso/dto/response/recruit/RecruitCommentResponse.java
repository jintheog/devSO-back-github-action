package com.example.devso.dto.response.recruit;

import com.example.devso.entity.recruit.RecruitComment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RecruitCommentResponse {
    private Long id;
    private String content;
    private RecruitUserResponse author;
    private LocalDateTime createdAt;

    @JsonProperty("isOwner")
    private boolean isOwner;

    // 댓글 계층
    // [{댓글1, children: [{대댓글A}, {대댓글B}]}, {댓글2, children: []}]
    // 1. 자식 댓글(대댓글) 리스트 추가
    @Builder.Default
    private List<RecruitCommentResponse> children = new ArrayList<>();

    // 2. 부모 ID
    private Long parentId;

    public static RecruitCommentResponse from(RecruitComment comment, Long currentUserId) {
        return RecruitCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(RecruitUserResponse.from(comment.getUser()))
                .createdAt(comment.getCreatedAt())
                .isOwner(comment.getUser().getId().equals(currentUserId))
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .children(comment.getChildren() != null ?
                        comment.getChildren().stream()
                                .map(child -> RecruitCommentResponse.from(child, currentUserId))
                                .sorted(Comparator.comparing(RecruitCommentResponse::getCreatedAt))
                                .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }
}