package com.example.moabackend.domain.report.dto.res;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;

public record DailyReportQuizScoreDto(
        EQuizType type,
        Integer correctNumber,
        Integer totalNumber) {
}