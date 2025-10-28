package com.example.moabackend.domain.user.controller;

import com.example.moabackend.domain.user.dto.SignUpConfirmationRequest;
import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserRoleSelectionRequest;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.service.UserService;
import com.example.moabackend.global.annotation.UserId;
import com.example.moabackend.global.code.ApiResponse;
import com.example.moabackend.global.code.GlobalSuccessCode;
import com.example.moabackend.global.security.dto.JwtDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Tag(name = "회원가입 1단계: 회원가입 시작 및 SMS 발송")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<ApiResponse<String>> preSignUpAndSendCode(@Valid @RequestBody UserSignUpRequest request) {
        userService.preSignUpAndSendCode(request);
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, "인증코드가 발송되었습니다.");
    }

    @Tag(name = "회원가입 2단계: 최종 회원가입 및 토큰 발행")
    @PostMapping("/signup/{phoneNumber}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<JwtDTO>> confirmSignUp(
            @PathVariable @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.") String phoneNumber,
            @Valid @RequestBody SignUpConfirmationRequest request) {
        JwtDTO jwt = userService.confirmSignUpAndLogin(phoneNumber, request.authCode());
        return ApiResponse.success(GlobalSuccessCode.CREATED, jwt);
    }

    @Tag(name = "회원가입 3단계: 역할 선택")

    @PostMapping("/select-role")
    public ResponseEntity<ApiResponse<UserResponseDto>> selectUserRole(
            @UserId Long userId,
            @Valid @RequestBody UserRoleSelectionRequest request) {

        UserResponseDto response = userService.selectRoleAndLinkParent(userId, request.getRole(), request.getParentCode());
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, response);
    }
}