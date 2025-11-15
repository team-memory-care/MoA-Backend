package com.example.moabackend.domain.quiz.dto.res;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;

public record QuizSubmitResponseDto(
        Long questionId,
        EQuizType quizType,
        boolean isCorrect,
        String correctAnswer
) {
}
