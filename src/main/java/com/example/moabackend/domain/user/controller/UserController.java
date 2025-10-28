package com.example.moabackend.domain.user.controller;

import com.example.moabackend.domain.user.dto.SignUpConfirmationRequest;
import com.example.moabackend.domain.user.dto.UserResponseDto;
import com.example.moabackend.domain.user.dto.UserRoleSelectionRequest;
import com.example.moabackend.domain.user.dto.UserSignUpRequest;
import com.example.moabackend.domain.user.service.UserService;
import com.example.moabackend.global.code.ApiResponse;
import com.example.moabackend.global.code.GlobalSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    /**
     * [회원가입 1단계]
     * 사용자가 입력한 기본 정보(DTO 전체)를 Redis에 임시 저장하고, 전화번호로 인증 코드 SMS를 발송 요청합니다.
     */
    @PostMapping("/sms/request")
    @ResponseStatus(HttpStatus.ACCEPTED) // 202 Accepted: 요청 수락
    public ResponseEntity<ApiResponse<String>> preSignUpAndSendCode(@Valid @RequestBody UserSignUpRequest request) {
        userService.preSignUpAndSendCode(request);
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, "인증코드가 발송되었습니다.");
    }

    /**
     * [회원가입 2단계]
     * 수신한 인증 코드를 검증하고, Redis의 임시 정보를 꺼내 DB에 최종적으로 사용자 계정을 생성합니다.
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<UserResponseDto>> confirmSignUp(
            @Valid @RequestBody SignUpConfirmationRequest request) {
        UserResponseDto response = userService.confirmSignUp(request.phoneNumber(), request.authCode());
        return ApiResponse.success(GlobalSuccessCode.CREATED, response);
    }

    /**
     * [역할 확정]
     * 계정 생성 후, PENDING 상태의 사용자가 최초로 로그인했을 때 PARENT나 CHILD 역할을 확정하고, 부모 코드를 연결합니다. (최초 1회 실행)
     */
    @PostMapping("/select-role")
    public ResponseEntity<ApiResponse<UserResponseDto>> selectUserRole(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRoleSelectionRequest request) {

        UserResponseDto response = userService.selectRoleAndLinkParent(userId, request.getRole(), request.getParentCode());
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, response);
    }
}