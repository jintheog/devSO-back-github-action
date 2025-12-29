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
    public RecruitResponse findById(Long recruitId, Long currentUserId) {
        Recruit recruit = recruitRepository.findByIdWithDetails(recruitId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUIT_NOT_FOUND));
        // 상세 조회에서만 조회수 증가
        recruit.increaseViewCount();
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
    public List<RecruitResponse> getFilteredRecruits(Long currentUserId, RecruitSearchRequest cond) {

        // 1. Integer 파라미터를 Enum으로 변환 (방어 로직 추가)
        // 값이 없거나 0(전체)인 경우 null을 할당하여 쿼리에서 무시되도록 합니다.
        RecruitType type = (cond.getType() == null || cond.getType() == 0)
                ? null
                : RecruitType.fromValue(cond.getType());

        RecruitPosition position = (cond.getPosition() == null || cond.getPosition() == 0)
                ? null
                : RecruitPosition.fromValue(cond.getPosition());

        RecruitProgressType progressType = (cond.getProgressType() == null)
                ? null
                : RecruitProgressType.fromValue(cond.getProgressType());

        // 2. 검색어 정제 (빈 문자열 처리)
        String searchKeyword = (cond.getSearch() == null || cond.getSearch().trim().isEmpty())
                ? null
                : cond.getSearch();

        // 3. 스택 리스트 변환
        List<TechStack> stacks = (cond.getStacks() == null || cond.getStacks().isEmpty())
                ? null
                : cond.getStacks().stream().map(TechStack::fromValue).toList();

        // 4. JPQL 호출 (1차 필터링)
        // Repository의 쿼리에서 :type IS NULL 조건을 타게 되어 전체 조회가 가능해집니다.
        List<Recruit> recruits = recruitRepository.findRecruitsByFilters(
                type,
                searchKeyword,
                stacks,
                position,
                progressType,
                cond.isOnlyOpen()
        );

        // 데이터가 없으면 즉시 반환
        if (recruits.isEmpty()) {
            return Collections.emptyList();
        }

        // 5. 북마크 여부 일괄 조회 (로그인한 경우에만 수행)
        Set<Long> bookmarkedIds = new HashSet<>();
        if (currentUserId != null) {
            List<Long> recruitIds = recruits.stream().map(Recruit::getId).toList();
            bookmarkedIds.addAll(
                    recruitBookMarkRepository.findRecruitIdsByUserIdAndRecruitIds(currentUserId, recruitIds)
            );
        }

        // 6. 최종 DTO 변환 및 '관심 목록/내 글' 필터링 (2차 필터링)
        return recruits.stream()
                .map(r -> RecruitResponse.from(r, bookmarkedIds.contains(r.getId())))
                .filter(res -> {
                    // 관심 목록 필터: 선택되었을 때 북마크가 안 된 글은 제외
                    if (cond.isOnlyBookmarked() && !res.isBookmarked()) {
                        return false;
                    }

                    // 내 모집글 필터: 선택되었을 때 작성자명이 다르면 제외
                    if (cond.isOnlyMyRecruits()) {
                        if (cond.getCurrentUsername() == null || !res.getUsername().equals(cond.getCurrentUsername())) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

}
