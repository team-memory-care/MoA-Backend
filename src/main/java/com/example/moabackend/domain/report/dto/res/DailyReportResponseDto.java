package com.example.moabackend.domain.report.dto.res;

import java.time.LocalDate;
import java.util.List;

public record DailyReportResponseDto(
        LocalDate date,
        List<DailyReportQuizScoreDto> scores,
        DailyAdviceListDto adviceList) {
}
