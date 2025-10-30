package com.example.moabackend.global.code;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlobalErrorCode implements ErrorCode {
    /**
     * 400 : 요청 실패 (Bad Request)
     */
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    BAD_JSON(HttpStatus.BAD_REQUEST, "요청 형식이 잘못되었거나 JSON 구조가 유효하지 않습니다."),
    MISSING_REQUEST_HEADER(HttpStatus.BAD_REQUEST, "필수 요청 헤더가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 값의 타입이 올바르지 않습니다."),
    INValid(HttpStatus.BAD_REQUEST, "잘못된 요청 헤더입니다."),

    /**
     * 401 : 인증 실패 (Authentication)
     */
    ACCESS_DENIED_ERROR(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    INVALID_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EMPTY_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."),
    TOKEN_MALFORMED_ERROR(HttpStatus.UNAUTHORIZED, "토큰이 올바르지 않습니다."),
    TOKEN_TYPE_ERROR(HttpStatus.UNAUTHORIZED, "토큰 타입이 일치하지 않거나 비어있습니다."),
    TOKEN_UNSUPPORTED_ERROR(HttpStatus.UNAUTHORIZED, "지원하지않는 토큰입니다."),
    EXPIRED_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_UNKNOWN_ERROR(HttpStatus.UNAUTHORIZED, "알 수 없는 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "자격 증명이 이루어지지 않았습니다."), // NOTE: 권한 부족은 403 FORBIDDEN 권장

    // 인증번호 만료
    AUTH_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),

    /**
     * 403 : 권한 부족 (Authorization)
     */
    INVALID_HEADER_VALUE(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    /**
     * 404 : 리소스 없음 (Not Found)
     */
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    NOT_SUPPORTED_URI_ERROR(HttpStatus.NOT_FOUND, "지원하지 않는 URL 입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 데이터입니다."),
    /**
     * 405 : 지원하지 않는 HTTP Method (Method Not Allowed)
     */
    NOT_SUPPORTED_METHOD_ERROR(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP Method 요청입니다."),

    /**
     * 409 : 충돌 (Conflict)
     */
    CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, "데이터베이스 제약 조건에 위배되었습니다."), // NOTE: 400 -> 409 CONFLICT 권장
    ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다."), // NOTE: 400 -> 409 CONFLICT 권장

    /**
     * 500 : 응답 실패 (Internal Server Error)
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버와의 연결에 실패했습니다."),
    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 작업 중 오류가 발생했습니다."),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "다른 서버로부터 잘못된 응답이 수신되었습니다."),
    INSUFFICIENT_STORAGE(HttpStatus.INSUFFICIENT_STORAGE, "서버의 용량이 부족해 요청에 실패했습니다."),
    UNSUPPORTED_ENCODING(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 인코딩입니다."),
    ;

    // 고유 코드 생성 실패 (서버 내부 문제)
    CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "고유 코드 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}