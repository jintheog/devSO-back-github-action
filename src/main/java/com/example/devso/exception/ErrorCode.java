package com.example.devso.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    OAUTH_TOKEN_FAILED(HttpStatus.UNAUTHORIZED, "OAUTH_TOKEN_FAILED", "토큰 발행 실패"),
    OAUTH_USER_INFO_FAILED(HttpStatus.UNAUTHORIZED, "OAUTH_USER_FAILED", "소셜유저 정보조회 실패"),

    // Auth
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "DUPLICATE_USERNAME", "이미 사용 중인 사용자명입니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다"),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", "사용자명 또는 비밀번호가 올바르지 않습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
    NOT_PROFILE_OWNER(HttpStatus.FORBIDDEN, "NOT_PROFILE_OWNER", "본인의 프로필만 수정할 수 있습니다"),

    // Post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시물을 찾을 수 없습니다"),
    NOT_POST_OWNER(HttpStatus.FORBIDDEN, "NOT_POST_OWNER", "게시물 작성자만 수정/삭제할 수 있습니다"),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다"),
    NOT_COMMENT_OWNER(HttpStatus.FORBIDDEN, "NOT_COMMENT_OWNER", "댓글 작성자만 수정/삭제할 수 있습니다"),

    // Like
    ALREADY_LIKED(HttpStatus.CONFLICT, "ALREADY_LIKED", "이미 좋아요한 게시물입니다"),
    NOT_LIKED(HttpStatus.BAD_REQUEST, "NOT_LIKED", "좋아요하지 않은 게시물입니다"),

    // Follow
    ALREADY_FOLLOWING(HttpStatus.CONFLICT, "ALREADY_FOLLOWING", "이미 팔로우한 사용자입니다"),
    NOT_FOLLOWING(HttpStatus.BAD_REQUEST, "NOT_FOLLOWING", "팔로우하지 않은 사용자입니다"),
    CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "CANNOT_FOLLOW_SELF", "자기 자신을 팔로우할 수 없습니다"),

    // File
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "지원하지 않는 파일 형식입니다"),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 올바르지 않습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다"),

    //Recruit
    RECRUIT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUIT_NOT_FOUND", "팀원 모집글을 찾을 수 없습니다"),
    NOT_RECRUIT_OWNER(HttpStatus.FORBIDDEN, "NOT_RECRUIT_OWNER", "본인의 팀원 모집글만 수정할 수 있습니다"),
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "INVALID_ENUM_VALUE", "올바르지 않은 enum 값입니다"),

    // AI
    AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_001", "AI 분석 중 오류가 발생했습니다"),
    AI_PROMPT_ERROR(HttpStatus.BAD_REQUEST, "AI_002", "분석을 위한 모집글 정보가 부족합니다"),
    AI_UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "AI_AUTH_001", "AI 사용 시 인증이 필요한 서비스입니다"),
    CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_003", "해당 공고에 대한 생성된 체크리스트가 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
