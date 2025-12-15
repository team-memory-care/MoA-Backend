package com.example.moabackend.global.exception;

import com.example.moabackend.global.code.ErrorCode;
import lombok.Getter;

@Getter
public class OpenAiRateLimitException extends RuntimeException {
    private final ErrorCode errorCode;

    public OpenAiRateLimitException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
