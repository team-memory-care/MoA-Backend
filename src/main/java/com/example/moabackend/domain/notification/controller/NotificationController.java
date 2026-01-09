package com.example.moabackend.domain.notification.controller;

import com.example.moabackend.domain.notification.code.success.NotificationSuccessCode;
import com.example.moabackend.domain.notification.dto.res.NotificationResponseDto;
import com.example.moabackend.domain.notification.service.NotificationService;
import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.annotation.UserId;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("")
    public BaseResponse<List<NotificationResponseDto>> findAllNotification(
            @UserId Long userId
    ) {
        return BaseResponse.success(NotificationSuccessCode.FIND_ALL_NOTIFICATION, notificationService.findAllNotification(userId));
    }

    @GetMapping("/not-read")
    public BaseResponse<Integer> countIsNotReadNotification(
            @UserId Long userId
    ) {
        return BaseResponse.success(NotificationSuccessCode.COUNT_NOT_READ_NOTIFICATION, notificationService.countIsNotReadNotification(userId));
    }

    @PatchMapping("{notificationId}")
    public BaseResponse<Void> setIsReadTrue(
            @PathVariable Long notificationId
    ) {
        notificationService.setNotificationToRead(notificationId);
        return BaseResponse.success(NotificationSuccessCode.CHANGE_NOTIFICATION_IS_READ_TRUE, null);
    }
}
