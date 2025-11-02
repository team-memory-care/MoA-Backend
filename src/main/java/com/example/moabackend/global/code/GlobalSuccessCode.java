package com.example.moabackend.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalSuccessCode implements SuccessCode {

    SUCCESS(HttpStatus.OK, "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "요청에 성공했으며 리소스가 정상적으로 생성되었습니다."),
    ACCEPTED(HttpStatus.ACCEPTED, "요청에 성공했으나 처리가 완료되지 않았습니다."),

    USER_REGISTER_TEMP_SAVED(HttpStatus.ACCEPTED, "회원가입 기본 정보가 임시 저장되었습니다."),
    AUTH_CODE_SENT(HttpStatus.OK, "인증코드가 발송되었습니다. 유효시간 5분.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
