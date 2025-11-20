package com.example.moabackend.domain.quiz.dto.res.result;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;

public record QuizSubmitResponseDto(
        Long questionId,
        EQuizType quizType,
        boolean isCorrect,
        String correctAnswer
) {
}
