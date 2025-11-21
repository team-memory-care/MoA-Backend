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

            "/api/v1/users/signup",
            "/api/v1/users/signup/sms",
            "/api/v1/users/role/parent",
            //"/api/v1/users/role/child",
            "/api/v1/users/parent-code",
            "/api/v1/users/withdraw",

            "/api/v1/auth/token/reissue",
            "/api/v1/auth/sms/request",
            "/api/v1/auth/logout",
            "/api/v1/auth/login",

            "/api/v1/quiz/submit",
            "/api/v1/quiz/result",
            "/api/v1/quiz/today",
            "/api/v1/quiz/set",
            "/api/v1/quiz/remain",
            "/api/v1/s3/upload"
    );
}
