package com.example.moabackend.domain.quiz.code.error;

import com.example.moabackend.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum QuizErrorCode implements ErrorCode {

    // 퀴즈 타입이 잘못된 경우
    INVALID_QUIZ_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 퀴즈 타입입니다."),

    // 퀴즈를 찾을 수 없는 경우
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈를 찾을 수 없습니다."),

    // 퀴즈를 타입이 잘못된 경우
    QUIZ_DATA_FORMAT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "문제 데이터 포맷 오류 (JSON 파싱 실패 등, 서버 데이터 문제)");

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