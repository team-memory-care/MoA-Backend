package com.example.moabackend.domain.quiz.dto.req;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;

public record QuizSaveRequestDto(
        int totalNumber,
        int correctNumber,
        EQuizType type
) {
}
