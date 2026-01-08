package com.example.moabackend.domain.notification.dto;

import com.example.moabackend.domain.report.entity.type.EReportType;

import java.time.LocalDate;

public record NotificationPayload(
        Long userId,
        EReportType reportType,
        LocalDate date
) {
}
