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

    public static final String S3_URL_PREFIX = "https://moa-bucket-s3.s3.ap-northeast-2.amazonaws.com/";

    // Notification Messages
    public static final String DAILY_REPORT_TITLE = "%s님의 오늘 인지변화를 확인해보세요";
    public static final String DAILY_REPORT_BODY = "오늘의 인지퀴즈 결과가 한눈에 보이도록 정리됐어요";

    public static final String WEEKLY_REPORT_TITLE = "%s님의 이번 주 인지퀴즈 기록은 어땠을까요?";
    public static final String WEEKLY_REPORT_BODY = "이번 주와 저번 주의 인지퀴즈 결과 차이를 확인해보세요";

    public static final String MONTHLY_REPORT_TITLE = "%s님의 이번 달 인지변화를 확인해보세요";
    public static final String MONTHLY_REPORT_BODY = "한 달간의 퀴즈 기록을 모아 전반적인 변화 흐름을 정리했어요";
}
