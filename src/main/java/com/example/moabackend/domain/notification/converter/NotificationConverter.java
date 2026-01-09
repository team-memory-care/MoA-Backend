package com.example.moabackend.domain.notification.converter;

import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.notification.dto.res.NotificationResponseDto;
import com.example.moabackend.domain.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConverter {
    public static NotificationResponseDto entityToDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getTitle(),
                notification.getBody(),
                notification.getReport().getId()
        );
    }

    public static NotificationResponseDto payloadToDto(NotificationPayload payload) {
        return new NotificationResponseDto(
                payload.title(),
                payload.body(),
                payload.reportId()
        );
    }
}
