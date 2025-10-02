package com.example.moabackend.global.token.controller;

import com.example.moabackend.global.code.ApiResponse;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.code.GlobalSuccessCode;
import com.example.moabackend.global.token.service.AuthServiceImpl;
import com.example.moabackend.global.token.service.RedisService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;
    private final RedisService redisService;

    /**
     * [2단계] 인증번호 발송
     * - 전화번호를 입력받아 인증번호를 생성
     * - Redis에 "auth:{phoneNumber}" 형태로 5분간 저장
     * - 실제 서비스에서는 SMS 발송 로직 필요
     * - 현재는 테스트용으로 응답에 인증번호를 포함
     */
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<String>> sendCode(@RequestParam @NotNull(message = "전화번호는 필수입니다.") String phoneNumber) {
        String code = authService.generateAuthCode(phoneNumber);
        redisService.setData("auth:" + phoneNumber, code, 5);
        return ApiResponse.success(GlobalSuccessCode.SUCCESS,
                "인증번호가 발송되었습니다. (테스트: " + code + ")");
    }

    /**
     * [3단계] 인증번호 검증
     * - 클라이언트가 입력한 인증번호와 Redis 저장값 비교
     * - 성공 시 Redis에 "verified:{phoneNumber}" = true 로 5분간 저장
     * - 실패 시 에러 응답 반환
     */
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<String>> verifyCode(@RequestParam String phoneNumber, @RequestParam String code) {
        boolean result = authService.verifyAuthCode(phoneNumber, code);

        if (result) {
            redisService.setData("verified:" + phoneNumber, true, 5);
            return ApiResponse.success(GlobalSuccessCode.SUCCESS, "인증 성공");
        } else {
            return ApiResponse.error(GlobalErrorCode.INVALID_AUTH_CODE);
        }
    }
}
