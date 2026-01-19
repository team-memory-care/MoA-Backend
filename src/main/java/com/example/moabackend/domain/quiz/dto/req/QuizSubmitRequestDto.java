package com.example.moabackend.domain.quiz.dto.req;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record QuizSubmitRequestDto(
        @NotNull Long questionId,
        @NotNull String userAnswer,
        LocalDate date) {
}
