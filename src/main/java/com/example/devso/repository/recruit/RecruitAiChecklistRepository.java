package com.example.devso.repository.recruit;

import com.example.devso.entity.recruit.RecruitAiChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruitAiChecklistRepository extends JpaRepository<RecruitAiChecklist, Long> {
    Optional<RecruitAiChecklist> findByRecruitIdAndUserId(Long recruitId, Long userId);
}
