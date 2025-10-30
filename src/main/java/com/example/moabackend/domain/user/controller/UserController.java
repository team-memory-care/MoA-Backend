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
import jakarta.validation.constraints.NotBlank;
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

    @Tag(name = "회원가입 1단계: 기본 정보 임시 저장")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<ApiResponse<String>> preSignUp(@Valid @RequestBody UserSignUpRequest request) {
        // 전화번호를 제외한 기본 정보를 Redis에 임시 저장하고, key를 반환합니다. (현재는 key 반환 생략)
        userService.preSignUp(request);
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, "회원가입 기본 정보가 임시 저장되었습니다.");
    }

    @Tag(name = "회원가입 2단계-1: 전화번호 인증 코드 발송")
    @PostMapping("/verify-phone/request")
    public ResponseEntity<ApiResponse<String>> requestSignUpSms(@Valid @RequestBody PhoneNumberRequest request) {
        // 전화번호 중복 체크 후, 인증 코드를 발송하고 Redis에 임시 저장합니다.
        userService.requestSignUpSms(request.phoneNumber());
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, "인증코드가 발송되었습니다. 유효시간 5분.");
    }

    @Tag(name = "회원가입 2단계-2: 인증 코드를 통한 최종 회원가입 및 토큰 발행")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<JwtDTO>> confirmSignUp(
            @Valid @RequestBody SignUpConfirmationRequest request) {
        // 인증 코드 검증 후, 임시 저장된 기본 정보와 전화번호를 이용해 DB에 최종 등록합니다.
        JwtDTO jwt = userService.confirmSignUpAndLogin(request.phoneNumber(), request.authCode());
        return ApiResponse.success(GlobalSuccessCode.CREATED, jwt);
    }

    @Tag(name = "회원가입 3단계: 역할 선택")
    @PostMapping("/select-role")
    public ResponseEntity<ApiResponse<UserResponseDto>> selectUserRole(
            @UserId Long userId,
            @Valid @RequestBody UserRoleSelectionRequest request) {

        UserResponseDto response = userService.selectRoleAndLinkParent(userId, request.role(), request.parentCode());
        return ApiResponse.success(GlobalSuccessCode.SUCCESS, response);
    }

    // 전화번호만 받는 DTO 정의 (클래스 분리도 가능)
    public record PhoneNumberRequest(
            @NotBlank(message = "전화번호는 필수 입력값입니다.")
            @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
            String phoneNumber
    ) {
    }
}