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

import java.util.Collections;
import java.util.List;

public record SpacetimeQuizQuestionDto(
        // 1. 공통 필드
        Long questionId,
        EQuizType quizType,
        String questionFormat,
        String questionContent,
        String answer,

        // 2. 유형별 필드
        String questionImageUrl,
        List<String> imageOptionsUrl
) implements QuizQuestionDto {

    // [1] 컴팩트 생성자: 데이터 검증
    public SpacetimeQuizQuestionDto {
        if (quizType == null || quizType != EQuizType.SPACETIME) {
            throw new CustomException(QuizErrorCode.INVALID_QUIZ_TYPE);
        }
    }

    // [2] 정적 팩터리 메서드: 변환
    public static SpacetimeQuizQuestionDto from(QuizQuestion entity, ObjectMapper objectMapper) {
        try {
            JsonNode jsonNode = objectMapper.readTree(entity.getDetailData());

            String rawQuestionImage = jsonNode.path("questionImageUrl").asText("");
            String processedQuestionImage = S3UrlUtils.convertToHttpUrl(rawQuestionImage);

            List<String> rawOptions = objectMapper.convertValue(
                    jsonNode.path("imageOptionsUrl"),
                    new TypeReference<List<String>>() {
                    });

            if (rawOptions == null) {
                rawOptions = Collections.emptyList();
            }

            List<String> processedOptions = rawOptions.stream()
                    .map(S3UrlUtils::convertToHttpUrl)
                    .toList();

            return new SpacetimeQuizQuestionDto(
                    entity.getId(),
                    entity.getType(),
                    entity.getQuestionFormat(),
                    entity.getQuestionContent(),
                    entity.getAnswer(),
                    processedQuestionImage,
                    processedOptions
            );
        } catch (JsonProcessingException e) {
            throw new CustomException(QuizErrorCode.QUIZ_DATA_FORMAT_ERROR);
        }
    }
}
