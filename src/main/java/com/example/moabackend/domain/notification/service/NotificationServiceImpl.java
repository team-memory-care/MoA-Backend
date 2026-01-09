package com.example.moabackend.domain.notification.service;

import com.example.moabackend.domain.notification.code.error.NotificationErrorCode;
import com.example.moabackend.domain.notification.converter.NotificationConverter;
import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.notification.dto.res.NotificationResponseDto;
import com.example.moabackend.domain.notification.entity.Notification;
import com.example.moabackend.domain.notification.repository.NotificationRepository;
import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.report.repository.ReportRepository;
import com.example.moabackend.domain.sse.dto.EMessageType;
import com.example.moabackend.domain.sse.service.SseEmitterService;
import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final SseEmitterService sseService;
    private final FcmService fcmService;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> findAllNotification(Long userId) {
        List<Notification> notifications = notificationRepository.findAllByUser_Id(userId);
        return notifications.stream()
                .map(NotificationConverter::entityToDto)
                .toList();
    }

    @Override
    @Transactional
    public void processNotification(NotificationPayload payload) {
        User user = userRepository.findById(payload.userId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Report report = reportRepository.findById(payload.reportId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND));

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .title(payload.title())
                        .body(payload.body())
                        .report(report)
                        .build()
        );

        sseService.sendToClient(payload.userId(), EMessageType.NOTIFICATION,
                NotificationConverter.payloadToDto(notification.getId(), payload, notification.getDateTime()));

        fcmService.sendMessage(user.getFcmToken(), payload.title(), payload.body(), payload.reportId());
    }

    @Override
    @Transactional(readOnly = true)
    public int countIsNotReadNotification(Long userId) {
        return notificationRepository.findNotReadNotification(userId);
    }

    @Override
    @Transactional
    public void setNotificationToRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        notification.setIsReadTrue();
    }
}
