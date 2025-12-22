package com.example.moabackend.domain.quiz.dto.res.question;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.util.S3UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public record MemoryQuizQuestionDto(
        // 1. 공통 필드
        Long questionId,
        EQuizType quizType,
        String questionFormat,
        String questionContent,
        List<String> answer,

        // 2. 유형별 필드
        List<String> imageUrls,
        String inputMethod,
        String requiredSequenceType

) implements QuizQuestionDto {

    // [1] 컴팩트 생성자: 데이터 검증
    public MemoryQuizQuestionDto {
        if (quizType == null || quizType != EQuizType.MEMORY) {
            throw new CustomException(QuizErrorCode.INVALID_QUIZ_TYPE);
        }
    }

    // [2] 정적 팩터리 메서드: 변환
    public static MemoryQuizQuestionDto fromList(List<QuizQuestion> entities, ObjectMapper objectMapper) {
        if (entities == null || entities.isEmpty()) {
            throw new CustomException(QuizErrorCode.QUIZ_NOT_FOUND);
        }

        // 1. 각 엔티티의 'image_url'을 추출하여 리스트화
        List<String> fullImageUrls = entities.stream().map(e -> {
            try {
                JsonNode node = objectMapper.readTree(e.getDetailData());
                return S3UrlUtils.convertToHttpUrl(node.path("image_url").asText());
            } catch (Exception ex) {
                return " ";
            }
        }).filter(url -> !url.isEmpty()).toList();

        // 2. 정답을 리스트로 수집 (예: ["사과", "의자", "당근"])
        List<String> combinedAnswer = entities.stream().map(QuizQuestion::getAnswer).toList();

        QuizQuestion first = entities.get(0);
        return new MemoryQuizQuestionDto(
                first.getId(), // 대표 ID
                first.getType(),
                first.getQuestionFormat(),
                "방금 나온 그림들을 순서대로 말씀해주세요!",
                combinedAnswer,
                fullImageUrls,
                "VOICE", "SEQUENCE"
        );
    }

    public static MemoryQuizQuestionDto from(QuizQuestion entity, ObjectMapper objectMapper) {
        try {
            JsonNode jsonNode = objectMapper.readTree(entity.getDetailData());

            List<String> rawKeys = objectMapper.convertValue(jsonNode.path("imageUrls"), new TypeReference<List<String>>() {
            });

            List<String> fullImageUrls = (rawKeys == null) ? List.of() : rawKeys.stream().map(S3UrlUtils::convertToHttpUrl).toList();

            return new MemoryQuizQuestionDto(
                    entity.getId(),
                    entity.getType(),
                    entity.getQuestionFormat(),
                    entity.getQuestionContent(),
                    List.of(entity.getAnswer()),
                    fullImageUrls,
                    jsonNode.path("input_method").asText(),
                    jsonNode.path("required_sequence_type").asText());
        } catch (JsonProcessingException e) {
            throw new CustomException(QuizErrorCode.QUIZ_DATA_FORMAT_ERROR);
        }
    }
}
