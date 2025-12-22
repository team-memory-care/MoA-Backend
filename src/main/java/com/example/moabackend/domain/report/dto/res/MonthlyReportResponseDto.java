package com.example.moabackend.domain.report.dto.res;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;

import java.util.List;
import java.util.Map;

public record MonthlyReportResponseDto(
        String oneLineReview,
        int completeRate,
        int correctRate,
        String diagnosis,
        Map<EQuizType, List<MonthlyScoreDto>> scores,
        String longTermStrategy) {
}
