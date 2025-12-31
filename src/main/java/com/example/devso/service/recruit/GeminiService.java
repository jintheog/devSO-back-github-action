package com.example.devso.service.recruit;

import com.example.devso.entity.User;
import com.example.devso.entity.recruit.Recruit;
import com.example.devso.entity.recruit.RecruitAiChecklist;
import com.example.devso.exception.CustomException;
import com.example.devso.exception.ErrorCode;
import com.example.devso.repository.UserRepository;
import com.example.devso.repository.recruit.RecruitAiChecklistRepository;
import com.example.devso.repository.recruit.RecruitRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RecruitRepository recruitRepository;
    private final RecruitAiChecklistRepository checklistRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public String getOrGenerateChecklist(Long recruitId, Long userId, boolean isRefresh) {
        log.info("[AI Checklist] 가동 - recruitId: {}, userId: {}, refresh: {}", recruitId, userId, isRefresh);

        RecruitAiChecklist existing = checklistRepository.findByRecruitIdAndUserId(recruitId, userId)
                .orElse(null);

        if (!isRefresh && existing != null && existing.getScore() != null) {
            return addScoreToJson(existing.getAiResponse(), existing.getScore());
        }

        Recruit recruit = recruitRepository.findById(recruitId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECRUIT_NOT_FOUND));

        Client client = Client.builder().apiKey(this.apiKey).build();

        try {
            String prompt = buildPrompt(recruit);
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String aiResponse = extractPureJson(response.text());

            if (existing != null) {
                existing.setAiResponse(aiResponse);
                existing.setScore(0);
            } else {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                RecruitAiChecklist newChecklist = new RecruitAiChecklist();
                newChecklist.setUser(user);
                newChecklist.setRecruit(recruit);
                newChecklist.setAiResponse(aiResponse);
                newChecklist.setScore(0);
                checklistRepository.save(newChecklist);
            }
            return aiResponse;
        } catch (Exception e) {
            log.error("[AI Checklist] 에러 상세: {}", e.getMessage());
            if (existing != null) {
                return addScoreToJson(existing.getAiResponse(), existing.getScore() != null ? existing.getScore() : 0);
            }
            return getFallbackMessage(e.getMessage());
        }
    }

    @Transactional
    public String calculateAndSaveScore(Long recruitId, Long userId, List<String> userCheckedQuestions) {
        RecruitAiChecklist checklist = checklistRepository.findByRecruitIdAndUserId(recruitId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHECKLIST_NOT_FOUND));

        try {
            JsonNode root = objectMapper.readTree(checklist.getAiResponse());
            JsonNode questions = root.get("checkList");
            int totalScore = 0;

            if (questions.isArray()) {
                for (JsonNode node : questions) {
                    String questionText = node.get("question").asText();
                    int score = node.get("score").asInt();

                    if (userCheckedQuestions.contains(questionText)) {
                        totalScore += score;
                    }
                }
            }

            // 점수 업데이트
            checklist.setScore(totalScore);
            return addScoreToJson(checklist.getAiResponse(), totalScore);

        } catch (Exception e) {
            log.error("[AI Score] 점수 계산 및 저장 중 오류: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String addScoreToJson(String aiResponse, Integer score) {
        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            if (root instanceof ObjectNode) {
                ((ObjectNode) root).put("score", score != null ? score : 0);
            }
            return root.toString();
        } catch (Exception e) {
            return aiResponse;
        }
    }

    private String buildPrompt(Recruit recruit) {
        String cleanContent = recruit.getContent().replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();

        return String.format("""
        당신은 IT 전문 커리어 코치입니다. 공고를 분석해 지원자 적합도 점수(총 100점)를 산출하기 위한 체크리스트를 만드세요.

        [공고 제목]: %s
        [공고 내용]: %s

        지시 사항:
        1. 질문(question)은 지원자의 경험/역량을 '예/아니오'로 묻는 형태여야 합니다.
        2. 각 질문에 배점(score)을 할당하세요. 모든 질문의 score 합은 반드시 정확히 100점이 되어야 합니다.
        3. 문항은 핵심 역량 위주로 5개 내외로 구성하세요.
        4. 분류(target)는 [필수역량, 우대사항, 기술스택] 중 하나로 지정하세요.

        제약: JSON 외 텍스트 출력 금지. 마크다운 기호(```json) 금지.

        응답 형식:
        {
          "checkList": [
            {"question": "Java 개발 경험이 3년 이상입니까?", "target": "기술스택", "score": 30}
          ],
          "matchTip": "핵심 기술 스택을 강조한 포트폴리오를 준비하세요."
        }
        """, recruit.getTitle(), cleanContent);
    }

    private String extractPureJson(String text) {
        Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return matcher.group(0);
        return text.trim();
    }

    private String getFallbackMessage(String errorMsg) {
        return "{\"checkList\": [{\"question\": \"현재 AI 분석 서비스를 이용할 수 없습니다. (할당량 초과)\", \"target\": \"안내\", \"score\": 0}], \"matchTip\": \"나중에 다시 시도해 주세요.\"}";
    }
}