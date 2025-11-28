package com.example.moabackend.domain.report.code.error;

import com.example.moabackend.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {
    OPEN_AI_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "OpenAI 인증에 실패했습니다."),
    OPEN_AI_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "OpenAI 요청 수를 초과하였습니다."),
    OPEN_AI_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI 리포트 생성 중 오류가 발생했습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "리포트가 존재하지 않습니다."),
    REPORT_TYPE_NOT_FOUNT(HttpStatus.NOT_FOUND, "리포트 타입이 존재하지 않습니다.");

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
