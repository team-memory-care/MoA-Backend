package com.example.moabackend.domain.notification.controller;

import com.example.moabackend.domain.notification.code.success.NotificationSuccessCode;
import com.example.moabackend.domain.notification.dto.res.NotificationResponseDto;
import com.example.moabackend.domain.notification.service.NotificationService;
import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.annotation.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
