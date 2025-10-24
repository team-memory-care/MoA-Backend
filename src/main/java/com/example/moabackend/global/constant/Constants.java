package com.example.moabackend.global.constant;

import java.time.Duration;
import java.util.List;

public class Constants {
    public static String CLAIM_USER_ID = "uuid";
    public static String CLAIM_USER_ROLE = "role";
    public static String REFRESH_COOKIE_NAME = "refresh_token";

    public static List<String> NO_NEED_AUTH = List.of(
            "/api/v1/health-check",
            "/swagger",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/api/auth/reissue",
            "/api/auth/logout",
            "/api/users/signup",
            "/api/users/signup-confirm",
            "/api/users/login",
            "/api/auth/send-code",
            "/api/auth/verify-code",
            "/ws/**"
    );
    public static final Duration SUBSCRIPTION_TTL = Duration.ofHours(24);
}
