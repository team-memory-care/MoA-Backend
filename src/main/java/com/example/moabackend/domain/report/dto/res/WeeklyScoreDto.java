package com.example.moabackend.domain.report.dto.res;

public record WeeklyScoreDto(
        String dayOfWeek,
        Long score,
        Long lastWeekScore) {
}