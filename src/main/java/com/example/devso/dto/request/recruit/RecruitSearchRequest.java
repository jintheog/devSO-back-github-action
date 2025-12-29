package com.example.devso.dto.request.recruit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class RecruitSearchRequest {

    private Integer type;          // 1: 스터디, 2: 프로젝트 (또는 Enum Ordinal)
    private String search;         // 검색어 (제목)
    private Integer position;      // 단일 선택된 포지션 ID
    private List<Integer> stacks;  // 다중 선택된 기술 스택 ID 리스트
    private Integer progressType;  // 진행 방식

    private boolean onlyOpen;      // 모집 중만 보기 여부 (true/false)
    private boolean onlyBookmarked; // 관심 목록만 보기 여부
    private boolean onlyMyRecruits;   // 내가 쓴 모집글 보기 여부

    private String currentUsername;
}