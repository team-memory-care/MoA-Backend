package com.example.moabackend.domain.notification.code.success;

import com.example.moabackend.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum NotificationSuccessCode implements SuccessCode {
    FIND_ALL_NOTIFICATION(HttpStatus.OK, "알림을 성공적으로 불러왔습니다."),
    COUNT_NOT_READ_NOTIFICATION(HttpStatus.OK, "읽지 않은 알림 개수를 성공적으로 불러왔습니다.")
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
