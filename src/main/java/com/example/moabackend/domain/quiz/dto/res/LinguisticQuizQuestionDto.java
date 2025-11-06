package com.example.moabackend.domain.quiz.dto.res;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.global.exception.CustomException;

import java.util.List;

public record LinguisticQuizQuestionDto(
        // 1. 공통 필드
        Long questionId,
        EQuizType quizType,
        String questionFormat,
        String questionContent,
        // 2. 유형별 필드
        List<String> options
) implements QuizQuestionDto {
    public LinguisticQuizQuestionDto {
        if (quizType != EQuizType.LINGUISTIC) {
            throw new CustomException(QuizErrorCode.INVALID_QUIZ_TYPE);
        }
    }
}
