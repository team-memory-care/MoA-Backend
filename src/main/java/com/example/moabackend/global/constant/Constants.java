package com.example.moabackend.global.constant;

import java.util.List;

public class Constants {
    public static String CLAIM_USER_ID = "uuid";
    public static String CLAIM_USER_ROLE = "role";
    public static String REFRESH_COOKIE_NAME = "refresh_token";
    public static String ACCESS_TOKEN_DENY = "access_token:deny:";

    public static List<String> NO_NEED_AUTH = List.of(
            "/api/v1/health-check",
            "/swagger",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/api/v1/auth/reissue",
            "/api/v1/auth/logout",
            "/api/v1/users/signup",
            "/api/v1/users/signup-confirm",
            "/api/v1/users/login",
            "/api/v1/users/register",
            "/api/v1/users/register/code-request",
            "/api/v1/users/register/code-complete",
            "/api/v1/auth/send-code",
            "/api/v1/auth/verify-code",
            "/api/v1/auth/sms/request",
            "/api/v1/auth/login",
            "/api/v1/auth/code/issue"
    );
}
