package com.example.moabackend.domain.quiz.dto.req;

import com.example.moabackend.domain.quiz.entity.type.EQuizCategory;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;

import java.time.LocalDate;

public record QuizSaveRequestDto(
        int totalNumber,
        int correctNumber,
        EQuizType type,
        EQuizCategory category,
        LocalDate date
) {
}
