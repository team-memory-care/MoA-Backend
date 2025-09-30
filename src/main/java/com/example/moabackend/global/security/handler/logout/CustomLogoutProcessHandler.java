package com.example.moabackend.global.security.handler.logout;

import com.example.moabackend.global.constant.Constants;
import com.example.moabackend.global.security.service.RefreshTokenService;
import com.example.moabackend.global.security.utils.CookieUtil;
import com.example.moabackend.global.security.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutProcessHandler implements LogoutHandler {
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = CookieUtil.getCookie(request, Constants.REFRESH_COOKIE_NAME);
        Long userId = jwtUtil.validateRefreshToken(refreshToken);
        refreshTokenService.deleteRefreshToken(userId);
    }
}
