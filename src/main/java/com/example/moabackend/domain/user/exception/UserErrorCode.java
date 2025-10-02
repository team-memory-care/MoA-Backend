package com.example.moabackend.domain.user.exception;

import com.example.moabackend.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    INVALID_USER(HttpStatus.BAD_REQUEST, "유효하지 않은 유저입니다."),
    DELETE_USER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "유저 정보를 삭제할 수 없습니다.");

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

    @Override
    public String getCode(){
        return this.name();
    }
}