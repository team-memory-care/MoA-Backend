package com.example.moabackend.domain.user.controller;

import com.example.moabackend.domain.user.dto.*;
import com.example.moabackend.domain.user.service.UserService;
import com.example.moabackend.global.annotation.UserId;
import com.example.moabackend.global.code.ApiResponse;
import com.example.moabackend.global.code.GlobalSuccessCode;
import com.example.moabackend.global.security.dto.JwtDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Tag(name = "회원가입 1단계: 기본 정보 임시 저장")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<ApiResponse<String>> preSignUp(@Valid @RequestBody UserSignUpRequest request) {
        // 1단계: 기본 정보와 전화번호를 Redis에 임시 저장 (전화번호를 Redis 키로 사용)
        userService.preSignUp(request);
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, "회원가입 기본 정보가 임시 저장되었습니다.");
    }

    @Tag(name = "회원가입 2단계-1: 전화번호 인증 코드 발송")
    @PostMapping("/register/code-request")
    public ResponseEntity<ApiResponse<String>> requestSignUpSms(@Valid @RequestBody PhoneNumberRequest request) {
        // 2-1단계: 전화번호 중복 체크 후, 회원가입용 인증 코드를 발송
        userService.requestSignUpSms(request.phoneNumber());
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, "인증코드가 발송되었습니다. 유효시간 5분.");
    }

    @Tag(name = "회원가입 2단계-2: 인증 코드를 통한 최종 회원가입 및 토큰 발행")
    @PostMapping("/register/code-complete")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<JwtDTO>> confirmSignUp(
            @Valid @RequestBody SignUpConfirmationRequest request) {
        // 2-2단계: 인증 코드 검증 및 Redis 임시 데이터로 DB에 최종 사용자 생성, JWT 토큰 발행
        JwtDTO jwt = userService.confirmSignUpAndLogin(request.phoneNumber(), request.authCode());
        return ApiResponse.success(GlobalSuccessCode.CREATED, jwt);
    }

    @Tag(name = "회원가입 3단계: 역할 선택")
    @PostMapping("/register/select-role")
    public ResponseEntity<ApiResponse<UserResponseDto>> selectUserRole(
            @UserId Long userId,
            @Valid @RequestBody UserRoleSelectionRequest request) {
        // 3단계: 인증된 사용자(userId)의 역할(PARENT/CHILD) 확정 및 부모-자녀 연결
        UserResponseDto response = userService.selectRoleAndLinkParent(userId, request.role(), request.parentCode());
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, response);
    }
}