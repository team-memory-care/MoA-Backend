package com.example.moabackend.domain.report.dto.res;

public record MonthlyScoreDto(
        int weekIndex,
        long score,
        long lastMonthScore
) {
}
