package com.example.moabackend.domain.notification.service;

import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.notification.dto.res.NotificationResponseDto;

import java.util.List;

public interface NotificationService {
    List<NotificationResponseDto> findAllNotification(Long userId);

    void processNotification(NotificationPayload payload);
}
