package com.example.devso.controller.recruit;

import com.example.devso.dto.request.recruit.RecruitCommentRequest;
import com.example.devso.dto.request.recruit.RecruitSearchRequest;
import com.example.devso.dto.response.recruit.RecruitCommentResponse;
import com.example.devso.dto.response.recruit.StackResponse;
import com.example.devso.exception.CustomException;
import com.example.devso.exception.ErrorCode;
import com.example.devso.security.CustomUserDetails;
import com.example.devso.dto.request.recruit.RecruitRequest;
import com.example.devso.dto.response.ApiResponse;
import com.example.devso.dto.response.recruit.EnumResponse;
import com.example.devso.dto.response.recruit.RecruitResponse;
import com.example.devso.entity.recruit.*;
import com.example.devso.service.recruit.GeminiService;
import com.example.devso.service.recruit.RecruitCommentService;
import com.example.devso.service.recruit.RecruitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Recruit", description = "팀원 모집 API")
@RestController
@RequestMapping("/api/recruits")
@RequiredArgsConstructor
public class RecruitController {
    private final RecruitService recruitService;
    private final RecruitCommentService recruitCommentService;
    private final GeminiService geminiService;

    @Operation(summary = "모집글 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<RecruitResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecruitRequest request
    ){
        System.out.println(request.toString());
        RecruitResponse response = recruitService.create(userDetails.getId(),  request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

//    @Operation(summary = "모집글 전체 조회")
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<RecruitResponse>>> findAll(
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ){
//        Long userId = (userDetails != null) ? userDetails.getId() : null;
//        List<RecruitResponse> list = recruitService.findAll(userId);
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ApiResponse.success(list));
//    }

    @Operation(summary = "모집글 상세조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecruitResponse>> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        RecruitResponse response = recruitService.findById(id, userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "북마크")
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<Boolean>> toggleBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean bookmarked = recruitService.toggleBookmark(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(bookmarked));
    }

    @Operation(summary = "모집글 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecruitResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecruitRequest request
    ) {
        RecruitResponse response = recruitService.update(userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "모집글 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        recruitService.delete(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "모집글 상태 변경")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RecruitStatus>> toggleStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecruitStatus newStatus = recruitService.toggleStatus(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(newStatus));
    }

    @Operation(summary = "모집글 필터링 조회 (필터 및 검색 포함)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RecruitResponse>>> getRecruits(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RecruitSearchRequest searchRequest,
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = (userDetails != null) ? userDetails.getId() : null;

        // 내가 쓴 글 필터링용 유저명 세팅
        if (userDetails != null) {
            searchRequest.setCurrentUsername(userDetails.getUsername());
        }

        Page<RecruitResponse> responses = recruitService.getFilteredRecruits(userId, searchRequest, pageable);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }


    // RecruitComment
    @Operation(summary = "댓글 및 대댓글 생성")
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<RecruitCommentResponse>> createComment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecruitCommentRequest request
    ){
        // parentId는 request 객체 안에 담겨서 전달됨
        RecruitCommentResponse response = recruitCommentService.create(id, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "특정 게시물의 댓글 목록 조회 (계층 구조)")
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<RecruitCommentResponse>>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        // 서비스에서 최상위 댓글만 필터링하여 자식들을 포함한 리스트 반환
        List<RecruitCommentResponse> comments = recruitCommentService.findByRecruitId(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/{id}/comments/{commentId}")
    public ResponseEntity<ApiResponse<RecruitCommentResponse>> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecruitCommentRequest request
    ) {
        RecruitCommentResponse response = recruitCommentService.update(commentId, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "댓글 삭제 (논리 삭제)")
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Void> deleteComments(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        recruitCommentService.delete(commentId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "AI 자가진단 체크리스트 생성 및 조회")
    @GetMapping("/{id}/ai-checklist")
    public ResponseEntity<ApiResponse<String>> getAiChecklist(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
    ) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.AI_UNAUTHORIZED_ACCESS);
        }

        String jsonResult = geminiService.getOrGenerateChecklist(id, userDetails.getId(), refresh);
        return ResponseEntity.ok(ApiResponse.success(jsonResult));
    }

    @Operation(summary = "AI 자가진단 점수 계산 및 결과 저장")
    @PostMapping("/{id}/ai-checklist/score")
    public ResponseEntity<ApiResponse<String>> calculateAiScore(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<String> checkedQuestions
    ) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.AI_UNAUTHORIZED_ACCESS);
        }

        // 반환 타입을 ApiResponse<Integer>에서 ApiResponse<String>으로 변경했습니다.
        String resultJson = geminiService.calculateAndSaveScore(id, userDetails.getId(), checkedQuestions);

        return ResponseEntity.ok(ApiResponse.success(resultJson));
    }


    // enum
    @Operation(summary = "포지션 enum 조회")
    @GetMapping("/enum/position")
    public ResponseEntity<List<EnumResponse>> getPositions() {
        List<EnumResponse> positions = Arrays.stream(RecruitPosition.values())
                .map(pos -> new EnumResponse(pos.getValue(), pos.getLabel(), pos.name()))
                .toList();
        return ResponseEntity.ok(positions);
    }

    @Operation(summary = "모집 타입 enum 조회")
    @GetMapping("/enum/type")
    public ResponseEntity<List<EnumResponse>> getTypes() {
        List<EnumResponse> types = Arrays.stream(RecruitType.values())
                .map(type -> new EnumResponse(type.getValue(), type.getLabel(), type.name()))
                .toList();
        return ResponseEntity.ok(types);
    }

    @Operation(summary = "진행 방식 enum 조회")
    @GetMapping("/enum/progress-type")
    public ResponseEntity<List<EnumResponse>> getProgress() {
        List<EnumResponse> progress = Arrays.stream(RecruitProgressType.values())
                .map(p -> new EnumResponse(p.getValue(), p.getLabel(), p.name()))
                .toList();
        return ResponseEntity.ok(progress);
    }

    @Operation(summary = "기술 스택 enum 조회")
    @GetMapping("/enum/tech-stacks")
    public ResponseEntity<List<StackResponse>> getTechStacks() {
        List<StackResponse> stacks = Arrays.stream(TechStack.values())
                .map(StackResponse::from)
                .toList();

        return ResponseEntity.ok(stacks);
    }

    @Operation(summary = "연락 방법 enum 조회")
    @GetMapping("/enum/contact")
    public ResponseEntity<List<EnumResponse>> getContactTypes() {
        List<EnumResponse> contactTypes = Arrays.stream(RecruitContactMethod.values())
                .map(c -> new EnumResponse(c.getValue(), c.getLabel(), c.name()))
                .toList();
        return ResponseEntity.ok(contactTypes);
    }

    @Operation(summary = "진행 기간 enum 조회")
    @GetMapping("/enum/duration")
    public ResponseEntity<List<EnumResponse>> getDurationTypes() {
        List<EnumResponse> durationTypes = Arrays.stream(RecruitDuration.values())
                .map(d -> new EnumResponse(d.getValue(), d.getLabel(), d.name()))
                .toList();
        return ResponseEntity.ok(durationTypes);
    }

    @Operation(summary = "모집 인원 enum 조회")
    @GetMapping("/enum/memberCount")
    public ResponseEntity<List<EnumResponse>> getMemberCount() {
        List<EnumResponse> memberCounts = Arrays.stream(RecruitCountOption.values())
                .map(m -> new EnumResponse(m.getValue(), m.getLabel(), m.name()))
                .toList();
        return ResponseEntity.ok(memberCounts);
    }
}
