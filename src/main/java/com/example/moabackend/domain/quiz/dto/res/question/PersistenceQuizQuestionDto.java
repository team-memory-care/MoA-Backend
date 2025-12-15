package com.example.moabackend.domain.quiz.dto.res.question;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.global.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record PersistenceQuizQuestionDto(
        // 1. 공통 필드
        Long questionId,
        EQuizType quizType,
        String questionFormat,
        String questionContent,
        String answer,

        // 2. 유형별 필드 (객관식)
        List<String> answerOptions) implements QuizQuestionDto {

    // [1] 컴팩트 생성자: 데이터 검증
    public PersistenceQuizQuestionDto {
        if (quizType == null || quizType != EQuizType.PERSISTENCE) {
            throw new CustomException(QuizErrorCode.INVALID_QUIZ_TYPE);
        }
    }

    // [2] 정적 팩터리 메서드: 변환
    public static PersistenceQuizQuestionDto from(QuizQuestion entity, ObjectMapper objectMapper) {
        try {
            JsonNode jsonNode = objectMapper.readTree(entity.getDetailData());

            List<String> options = objectMapper.convertValue(
                    jsonNode.path("answerOptions"),
                    new TypeReference<List<String>>() {
                    });

            if (options == null) {
                options = Collections.emptyList();
            }

            List<String> shuffledOptions = new ArrayList<>(options);
            Collections.shuffle(shuffledOptions);

            return new PersistenceQuizQuestionDto(
                    entity.getId(),
                    entity.getType(),
                    entity.getQuestionFormat(),
                    entity.getQuestionContent(),
                    entity.getAnswer(),
                    shuffledOptions);
        } catch (JsonProcessingException e) {
            throw new CustomException(QuizErrorCode.QUIZ_DATA_FORMAT_ERROR);
        }
    }
}
