package com.example.moabackend.domain.quiz.dto.res.question;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.global.exception.CustomException;

public record MemoryQuizQuestionDto(
        // 1. 공통 필드
        Long questionId,
        EQuizType quizType,
        String questionFormat,
        String questionContent,
        // 2. 유형별 필드
        String imageUrl,
        String inputMethod,
        String requiredSequenceType
) implements QuizQuestionDto {
    public MemoryQuizQuestionDto{
        if (quizType == null || quizType!=EQuizType.MEMORY){
            throw new CustomException(QuizErrorCode.INVALID_QUIZ_TYPE);
        }
    }
}
