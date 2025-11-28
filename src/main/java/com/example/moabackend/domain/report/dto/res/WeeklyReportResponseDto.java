package com.example.moabackend.domain.report.dto.res;

import com.example.moabackend.domain.quiz.entity.type.EQuizType;

import java.util.List;
import java.util.Map;

public record WeeklyReportResponseDto(
        String oneLineReview,
        int completeRate,
        int correctRate,
        Map<EQuizType, List<WeeklyScoreDto>> scores,
        String diagnosis,
        String nextWeekStrategy) {
}