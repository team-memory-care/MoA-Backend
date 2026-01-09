package com.example.moabackend.domain.notification.dto.res;

public record NotificationResponseDto(
        String title,
        String body,
        Long reportId,
        boolean isRead,
        String notificationTime
) {
}
