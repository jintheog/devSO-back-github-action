package com.example.devso.dto.request.recruit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecruitCommentRequest {
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 100, message = "댓글은 100자 이하여야 합니다.")
    private String content;

    // 대댓글 작성을 위한 부모 댓글 ID(일반 댓글: null, 답글: 부모 댓글 ID)
    private Long parentId;
}
