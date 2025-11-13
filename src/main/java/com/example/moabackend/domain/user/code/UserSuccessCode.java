package com.example.moabackend.domain.user.code;

import com.example.moabackend.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {
    USER_REGISTER_TEMP_SAVED(HttpStatus.ACCEPTED, "회원가입 기본 정보가 임시 저장되었습니다."),
    AUTH_CODE_SENT(HttpStatus.OK, "인증코드가 발송되었습니다. 유효시간 5분."),
    REISSUE_SUCCESS(HttpStatus.OK, "토큰을 재발급하였습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃에 성공하였습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
