package com.example.moabackend.domain.report.code.error;

import com.example.moabackend.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OpenAiErrorCode implements ErrorCode {
    OPEN_AI_TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "너무 많은 요청을 보냈습니다."),
    OPEN_AI_RETRY_AFTER_DELAY(HttpStatus.BAD_REQUEST, "요청을 다시 시도해주세요")
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
