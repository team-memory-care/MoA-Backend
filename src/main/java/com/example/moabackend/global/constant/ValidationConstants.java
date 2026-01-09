package com.example.moabackend.global.constant;

public final class ValidationConstants {
    private ValidationConstants() {
    }

    // 이름: 한글 또는 영문만 허용, 공백 절대 불가, 2~10자
    public static final String NAME_REGEX = "^[가-힣a-zA-Z]{2,10}$";
    public static final String NAME_MESSAGE = "이름은 공백 없이 한글 또는 영문 2~10자여야 합니다.";

    // 생년월일: 8자리 숫자
    public static final String BIRTHDATE_REGEX = "^\\d{8}$";
    public static final String BIRTHDATE_MESSAGE = "생년월일은 yyyyMMdd 형식의 8자리 숫자여야 합니다.";

    // 전화번호: 10~15자리 숫자 (국가코드 포함 권장)
    public static final String PHONE_REGEX = "^\\+?[0-9]{10,15}$";
    public static final String PHONE_MESSAGE = "전화번호는 10~15자리의 숫자여야 합니다.";

    // 인증 코드: 4자리 숫자
    public static final String AUTH_CODE_REGEX = "^\\d{4}$";
    public static final String AUTH_CODE_MESSAGE = "인증 코드는 4자리 숫자여야 합니다.";
}