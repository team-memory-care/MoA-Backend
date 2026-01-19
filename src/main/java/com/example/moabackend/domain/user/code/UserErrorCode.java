package com.example.moabackend.domain.user.code;

import com.example.moabackend.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    INVALID_USER(HttpStatus.BAD_REQUEST, "유효하지 않은 유저입니다."),
    DELETE_USER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "유저 정보를 삭제할 수 없습니다."),
    ALREADY_ROLE_SELECTED(HttpStatus.CONFLICT, "이미 역할을 선택했습니다."),
    USER_STATUS_INVALID(HttpStatus.FORBIDDEN, "로그인이 불가능한 상태의 계정입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "부모/자녀 외의 역할을 선택했습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 유저입니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않거나 만료되었습니다."),
    AUTH_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    INVALID_PARENT_CODE(HttpStatus.BAD_REQUEST, "입력된 부모 회원 코드를 가진 사용자가 존재하지 않습니다."),
    PARENT_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 코드를 찾을 수 없습니다."),
    PARENT_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "부모 코드가 이미 발급되었습니다."),
    CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "고유 코드 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.");

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