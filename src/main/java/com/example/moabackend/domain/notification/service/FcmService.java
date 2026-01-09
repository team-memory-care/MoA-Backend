package com.example.moabackend.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    public void sendMessage(String token, String title, String body, Long reportId) {
        if (token == null || token.isEmpty()) {
            log.warn("FCM token is empty, skipping.");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .putData("reportId", String.valueOf(reportId));

            Message message = messageBuilder.build();

            FirebaseMessaging.getInstance().send(message);
            log.info("FCM sent successfully to token: {}", token);

        } catch (Exception e) {
            log.error("FCM send failed: {}", e.getMessage());
        }
    }
}
