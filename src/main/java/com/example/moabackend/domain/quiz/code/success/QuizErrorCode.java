package com.example.moabackend.domain.quiz.code.success;

import com.example.moabackend.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum QuizErrorCode implements ErrorCode {

    // 퀴즈 타입이 잘못된 경우
    INVALID_QUIZ_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 퀴즈 타입입니다."),

    // 퀴즈를 찾을 수 없는 경우
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈를 찾을 수 없습니다."),
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