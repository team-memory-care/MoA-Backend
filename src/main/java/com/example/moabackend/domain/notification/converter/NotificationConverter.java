package com.example.moabackend.domain.notification.converter;

import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.notification.dto.res.NotificationResponseDto;
import com.example.moabackend.domain.notification.entity.Notification;
import com.example.moabackend.global.util.TimeFormatter;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationConverter {
    public static NotificationResponseDto entityToDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getTitle(),
                notification.getBody(),
                notification.getReport().getId(),
                false,
                TimeFormatter.formatTimeAgo(notification.getDateTime())
        );
    }

    public static NotificationResponseDto payloadToDto(NotificationPayload payload, LocalDateTime dateTime) {
        return new NotificationResponseDto(
                payload.title(),
                payload.body(),
                payload.reportId(),
                false,
                TimeFormatter.formatTimeAgo(dateTime)
        );
    }
}
