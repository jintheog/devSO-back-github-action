package com.example.devso.service.recruit;

import com.example.devso.dto.request.recruit.RecruitRequest;
import com.example.devso.dto.request.recruit.RecruitSearchRequest;
import com.example.devso.dto.response.recruit.RecruitResponse;
import com.example.devso.entity.User;
import com.example.devso.entity.recruit.*;
import com.example.devso.exception.CustomException;
import com.example.devso.exception.ErrorCode;
import com.example.devso.repository.recruit.RecruitBookMarkRepository;
import com.example.devso.repository.recruit.RecruitRepository;
import com.example.devso.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitService {
    private final RecruitRepository recruitRepository;
    private final RecruitBookMarkRepository recruitBookMarkRepository;
    private final UserRepository userRepository;

    // 모집글 생성
    @Transactional
    public RecruitResponse create(Long userId, RecruitRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Recruit recruit = Recruit.create(user, request);
        recruitRepository.save(recruit);
        return RecruitResponse.from(recruit);
    }

    //모집글 전체 조회
    public List<RecruitResponse> findAll(Long currentUserId){
        List<Recruit> recruits = recruitRepository.findAllWithUser();
        return recruits.stream().map(recruit -> toRecruitResponseWithStatus(recruit, currentUserId)).toList();
    }

    //모집글 상세 조회
    @Transactional
    public RecruitResponse findById(Long recruitId, Long currentUserId, boolean isIncrement) {
        Recruit recruit = recruitRepository.findByIdWithDetails(recruitId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUIT_NOT_FOUND));

        // true일 때만 조회수 증가
        if (isIncrement) {
            recruit.increaseViewCount();
        }
        return toRecruitResponseWithStatus(recruit, currentUserId);
    }

    //모집글 수정
    @Transactional
    public RecruitResponse update(Long userId, Long recruitId, RecruitRequest request) {
        Recruit recruit = recruitRepository.findById(recruitId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUIT_NOT_FOUND));

        if (!recruit.isOwner(userId)) {
            throw new CustomException(ErrorCode.NOT_RECRUIT_OWNER);
        }

        recruit.update(
                request.getTitle(),
                request.getContent(),
                request.getPositions(),
                request.getProgressType(),
                request.getDuration(),
                request.getContactMethod(),
                request.getContactInfo(),
                request.getStacks(),
                request.getTotalCount(),
                request.getDeadLine()
        );


        return RecruitResponse.from(recruit);
    }

    // 모집글 상태 토글(OPEN/CLOSE)
    @Transactional
    public RecruitStatus toggleStatus(Long userId, Long recruitId) {
        Recruit recruit = recruitRepository.findById(recruitId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUIT_NOT_FOUND));

        // 작성자 검증 (엔티티의 isOwner 메서드 활용)
        if (!recruit.isOwner(userId)) {
            throw new CustomException(ErrorCode.NOT_RECRUIT_OWNER);
        }

        // 상태 토글 로직
        if (recruit.getStatus() == RecruitStatus.OPEN) {
            recruit.close(); // 엔티티의 close() 메서드 호출
        } else {
            recruit.open();
        }

        return recruit.getStatus();
    }

    //모집글 삭제(작성자 본인만 가능)
    @Transactional
    public void delete(Long userId, Long recruitId) {
        Recruit recruit = recruitRepository.findById(recruitId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUIT_NOT_FOUND));

        if (!recruit.isOwner(userId)) {
            throw new CustomException(ErrorCode.NOT_RECRUIT_OWNER);
        }

        recruitRepository.delete(recruit);
    }



    //Recruit엔티티와 사용자 정보로 상태 반환
    private RecruitResponse toRecruitResponseWithStatus(Recruit recruit, Long currentUserId) {
        //북마크 표시
        boolean bookmarked = currentUserId != null
                && recruitBookMarkRepository.existsByUserIdAndRecruitId(currentUserId, recruit.getId());
        return RecruitResponse.from(recruit, bookmarked);
    }

    //북마크 토글
    @Transactional
    public boolean toggleBookmark(Long userId, Long recruitId) {
        Recruit recruit = recruitRepository.findById(recruitId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUIT_NOT_FOUND));

        boolean exists = recruitBookMarkRepository.existsByUserIdAndRecruitId(userId, recruitId);
        if (exists) {
            recruitBookMarkRepository.deleteByUserIdAndRecruitId(userId, recruitId);
            return false; // 북마크 해제
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            RecruitBookMark bookmark = new RecruitBookMark(user, recruit);
            recruitBookMarkRepository.save(bookmark);
            return true; // 북마크 등록
        }
    }

    @Transactional(readOnly = true)
    public Page<RecruitResponse> getFilteredRecruits(Long currentUserId, RecruitSearchRequest cond, Pageable pageable) {

        // 1. 파라미터 변환 (기존 로직 유지)
        RecruitType type = (cond.getType() == null || cond.getType() == 0) ? null : RecruitType.fromValue(cond.getType());
        RecruitPosition position = (cond.getPosition() == null || cond.getPosition() == 0) ? null : RecruitPosition.fromValue(cond.getPosition());
        RecruitProgressType progressType = (cond.getProgressType() == null) ? null : RecruitProgressType.fromValue(cond.getProgressType());
        String searchKeyword = (cond.getSearch() == null || cond.getSearch().trim().isEmpty()) ? null : cond.getSearch();
        List<TechStack> stacks = (cond.getStacks() == null || cond.getStacks().isEmpty()) ? null : cond.getStacks().stream().map(TechStack::fromValue).toList();

        // 2. DB 호출 (컨트롤러에서 채워진 currentUsername을 그대로 필터로 사용)
        Page<Recruit> recruitPage = recruitRepository.findRecruitsByFilters(
                type, searchKeyword, stacks, position, progressType,
                cond.isOnlyOpen(), cond.isOnlyBookmarked(), cond.isOnlyMyRecruits(),
                currentUserId, cond.getCurrentUsername(), RecruitStatus.OPEN , pageable
        );

        // 3. 북마크 여부 확인
        Set<Long> bookmarkedIds = new HashSet<>();
        if (currentUserId != null && !recruitPage.isEmpty()) {
            List<Long> ids = recruitPage.getContent().stream().map(Recruit::getId).toList();
            bookmarkedIds.addAll(recruitBookMarkRepository.findRecruitIdsByUserIdAndRecruitIds(currentUserId, ids));
        }

        // 4. Page<Entity> -> Page<DTO> 변환 (페이지 정보 유지됨)
        return recruitPage.map(r -> RecruitResponse.from(r, bookmarkedIds.contains(r.getId())));
    }

}
