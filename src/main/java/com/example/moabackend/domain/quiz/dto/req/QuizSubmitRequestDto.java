package com.example.moabackend.domain.quiz.dto.req;

import jakarta.validation.constraints.NotNull;

public record QuizSubmitRequestDto(
        @NotNull Long questionId,
        @NotNull String userAnswer) {
}
