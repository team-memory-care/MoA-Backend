package com.example.moabackend.global.token.controller;

import com.example.moabackend.domain.user.code.UserSuccessCode;
import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.code.GlobalSuccessCode;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.token.dto.req.LogoutRequestDto;
import com.example.moabackend.global.token.dto.req.ReissueTokenRequestDto;
import com.example.moabackend.global.token.dto.res.ReissueTokenResponseDto;
import com.example.moabackend.global.token.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "인증(Auth)", description = "로그인, 토큰 재발급 및 로그아웃 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인 SMS 요청", description = "기존 회원의 로그인을 위한 인증번호를 발송합니다.")
    @PostMapping("/sms/request")
    public BaseResponse<Void> requestLoginSms(
            @RequestParam @NotNull(message = "전화번호는 필수입니다.") String phoneNumber) {
        authService.generateAuthCode(phoneNumber);
        return BaseResponse.success(UserSuccessCode.AUTH_CODE_SENT, null);
    }

    @Operation(summary = "로그인", description = "인증번호 검증 후 액세스/리프레시 토큰을 발급합니다.")
    @PostMapping("/login")
    public BaseResponse<JwtDTO> login(
            @RequestParam @NotNull(message = "전화번호는 필수입니다.") String phoneNumber,
            @RequestParam @NotNull(message = "인증 코드는 필수입니다.") String authCode) {
        JwtDTO jwt = authService.login(phoneNumber, authCode);
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, jwt);
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용하여 Access Token을 갱신합니다.")
    @PostMapping("/token/reissue")
    public BaseResponse<ReissueTokenResponseDto> reissue(
            @RequestBody ReissueTokenRequestDto request
    ) {
        return BaseResponse.success(UserSuccessCode.REISSUE_SUCCESS, authService.reissueToken(request));
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 만료시키고 블랙리스트를 처리합니다.")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(
            @RequestBody LogoutRequestDto request
    ) {
        authService.logout(request);
        return BaseResponse.success(UserSuccessCode.LOGOUT_SUCCESS, null);
    }
}