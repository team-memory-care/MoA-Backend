package com.example.moabackend.global.constant;

import java.util.List;

public class Constants {
    public static String CLAIM_USER_ID = "uuid";
    public static String CLAIM_USER_ROLE = "role";
    public static String REFRESH_COOKIE_NAME = "refresh_token";
    public static String ACCESS_TOKEN_DENY = "access_token:deny:";

    public static List<String> NO_NEED_AUTH = List.of(
            // [1] 누구나 접근 가능한 시스템/문서 경로
            "/api/v1/health-check",
            "/swagger",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs",
            "/api-docs/**",
            "/v3/api-docs/**",

            // [2] 로그인/가입 전이라 토큰이 없는 경우
            "/api/v1/users/signup",
            "/api/v1/users/signup/sms",

            // [3] 인증 관련 (로그인, 재발급 등은 내부에서 검증하거나 토큰 없이 호출됨)
            "/api/v1/auth/token/reissue",
            "/api/v1/auth/sms/request",
            "/api/v1/auth/login",

            "/api/v1/s3/upload"
    );
}
