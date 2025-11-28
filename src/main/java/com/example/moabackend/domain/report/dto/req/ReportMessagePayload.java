package com.example.moabackend.domain.report.dto.req;

import java.time.LocalDate;

public record ReportMessagePayload(
        Long userId,
        LocalDate date,
        String reportType
) {
}
