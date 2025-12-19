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
        String answer,

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
                    entity.getAnswer(),
                    fullImageUrls,
                    jsonNode.path("input_method").asText(),
                    jsonNode.path("required_sequence_type").asText());
        } catch (JsonProcessingException e) {
            throw new CustomException(QuizErrorCode.QUIZ_DATA_FORMAT_ERROR);
        }
    }
}
