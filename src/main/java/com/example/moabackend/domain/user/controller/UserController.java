package com.example.moabackend.domain.user.controller;

import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserRoleSelectionRequest;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.service.UserService;
import com.example.moabackend.global.code.ApiResponse;
import com.example.moabackend.global.code.GlobalSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    /**
     * 회원가입 API: 사용자 기본 정보 입력 및 계정 생성
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signUp(@Valid @RequestBody UserSignUpRequest request) {
        UserResponseDto response = userService.signUp(request);
        return ApiResponse.success(GlobalSuccessCode.CREATED, response);
    }

    /**
     * 사용자 역할 선택 API: 로그인 후 역할 및 부모-자녀 연결 (최초 1회)
     */
    @PostMapping("/select-role")
    public ResponseEntity<ApiResponse<UserResponseDto>> selectUserRole(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRoleSelectionRequest request) {

        UserResponseDto response = userService.selectRoleAndLinkParent(userId, request.getRole(), request.getParentCode());
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, response);
    }
}