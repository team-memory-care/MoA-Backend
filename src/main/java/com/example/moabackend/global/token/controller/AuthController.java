package com.example.moabackend.global.token.controller;

import com.example.moabackend.domain.user.service.UserService;
import com.example.moabackend.global.code.ApiResponse;
import com.example.moabackend.global.code.GlobalSuccessCode;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.token.service.AuthServiceImpl;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;
    private final UserService userService;

    /**
     * 로그인 API: 전화번호 인증 후 JWT 토큰 발급.
     * - authCode가 없으면 인증번호를 발송하고, 있으면 검증 후 로그인 처리.
     */
    @PostMapping("/login")
    public ResponseEntity<? extends ApiResponse<? extends Object>> login(
            @RequestParam @NotNull(message = "전화번호는 필수입니다.") String phoneNumber,
            @RequestParam(required = false) String authCode) {
        Object result = authService.login(phoneNumber, authCode);

        if(result instanceof String){
            return ApiResponse.<String>success(GlobalSuccessCode.SUCCESS, (String) result);
        }
        return ApiResponse.<JwtDTO>success(GlobalSuccessCode.SUCCESS, (JwtDTO) result);
    }

    /**
     * 회원 코드 생성 API: 인증된 부모 사용자의 회원 코드를 조회하거나 새로 발급.
     */
    @PostMapping("/code/issue")
    public ResponseEntity<ApiResponse<String>> issueParentCode(@AuthenticationPrincipal Long userId) {
        String code = userService.issueOrGetParentCode(userId);
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, code);
    }
}