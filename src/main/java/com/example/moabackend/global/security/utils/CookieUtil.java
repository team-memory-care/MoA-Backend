package com.example.moabackend.global.security.utils;

import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.constant.Constants;
import com.example.moabackend.global.exception.CustomException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;

@Slf4j
public class CookieUtil {

    public static String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            log.info("[CookieUtil] request.getCookies() == null (쿠키가 전혀 없음)");
            throw new CustomException(GlobalErrorCode.INVALID_TOKEN_ERROR);
        }

        // 요청에 들어온 전체 쿠키 찍기
        Arrays.stream(request.getCookies())
                .forEach(cookie -> log.info("[CookieUtil] Incoming cookie: name={}, value={}", cookie.getName(), cookie.getValue()));

        // 원하는 쿠키 찾기
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> {
                    log.info("[CookieUtil] '{}' 쿠키를 찾지 못했습니다.", name);
                    return new CustomException(GlobalErrorCode.INVALID_TOKEN_ERROR);
                });
    }


    public static void addCookie(
            HttpServletResponse response,
            String domain,
            String key,
            String value
    ) {
        ResponseCookie cookie = ResponseCookie.from(key, value)
                .path("/")
                .domain(domain)
                .httpOnly(false)
                .secure(true)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void logoutCookie(
            HttpServletResponse response,
            String domain
    ) {
        ResponseCookie expiredCookie = ResponseCookie.from(Constants.REFRESH_COOKIE_NAME, "")
                .path("/")
                .domain(domain)
                .secure(true)       // HTTPS 필수
                .httpOnly(true)     // Refresh Token은 보안상 HttpOnly
                .maxAge(0)          // 즉시 만료
                .sameSite("None")   // 크로스 도메인 로그인/로그아웃 지원
                .build();

        log.info("[Cookie] Logout cookie set: {}", expiredCookie.toString());

        response.addHeader("Set-Cookie", expiredCookie.toString());
    }


    public static void addSecureCookie(
            HttpServletResponse response,
            String domain,
            String key,
            String value,
            Long maxAge
    ) {
        ResponseCookie cookie = ResponseCookie.from(key, value)
                .path("/")
                .domain(domain)
                .httpOnly(true)
                .secure(true)       // HTTPS 필수
                .sameSite("None")   // 교차 도메인 전송 지원
                .maxAge(maxAge)
                .build();

        log.info("[Cookie] Add secure cookie: {}", cookie.toString());

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            String name
    ) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return;

        for (Cookie cookie : cookies)
            if (cookie.getName().equals(name)) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
    }
}