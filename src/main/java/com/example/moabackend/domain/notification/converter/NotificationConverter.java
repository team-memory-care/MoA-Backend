package com.example.moabackend.domain.notification.converter;

import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.notification.dto.res.NotificationResponseDto;
import com.example.moabackend.domain.notification.entity.Notification;
import com.example.moabackend.global.util.TimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationConverter {
    public static NotificationResponseDto entityToDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.getReport().getId(),
                notification.getIsRead(),
                TimeFormatter.formatTimeAgo(notification.getDateTime())
        );
    }

    public static NotificationResponseDto payloadToDto(
            Long notificationId,
            NotificationPayload payload,
            LocalDateTime dateTime
    ) {
        return new NotificationResponseDto(
                notificationId,
                payload.title(),
                payload.body(),
                payload.reportId(),
                false,
                TimeFormatter.formatTimeAgo(dateTime)
        );
    }
}
