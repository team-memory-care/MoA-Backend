package com.example.moabackend.domain.report.dto.req;

import com.example.moabackend.domain.report.entity.type.EReportType;

import java.time.LocalDate;

public record ReportMessagePayload(
        Long userId,
        LocalDate date,
        EReportType reportType
) {
}
