package com.example.moabackend.domain.notification.dto;

import com.example.moabackend.domain.report.entity.type.EReportType;

public record NotificationPayload(
        Long userId,
        EReportType type,
        String title,
        String body,
        Long reportId
) {
}
