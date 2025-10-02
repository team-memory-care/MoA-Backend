package com.example.moabackend.domain.user.controller;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.service.UserService;
import com.example.moabackend.global.code.ApiResponse;
import com.example.moabackend.global.code.GlobalSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    /**
     * [1단계] 회원 정보 입력 (임시 저장)
     * - 사용자가 입력한 회원 정보를 Redis에 5분간 저장
     * - 인증번호 검증이 완료되기 전까지는 DB에 저장하지 않음
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> preSignup(@Valid @RequestBody UserSignUpRequest request) {
        userService.preSignup(request);
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, "회원 정보 임시 저장 완료");
    }

    /**
     * [4단계] 최종 회원가입 확정
     * - 인증번호 검증이 성공한 상태(verified:{phoneNumber}=true)인지 확인
     * - Redis에 저장된 임시 회원 정보를 DB에 영구 저장
     * - 성공 시 최종 회원 정보 반환
     */
    @PostMapping("/signup-confirm")
    public ResponseEntity<ApiResponse<UserResponseDto>> confirmSignup(@RequestParam String phoneNumber) {
        UserResponseDto response = userService.confirmSignup(phoneNumber);
        return ApiResponse.success(GlobalSuccessCode.CREATED, response);
    }
}
