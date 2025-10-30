package com.example.moabackend.domain.user.controller;

import com.example.moabackend.domain.user.code.UserSuccessCode;
import com.example.moabackend.domain.user.dto.req.PhoneNumberRequestDto;
import com.example.moabackend.domain.user.dto.req.SignUpConfirmationRequestDto;
import com.example.moabackend.domain.user.dto.req.UserRoleSelectionRequestDto;
import com.example.moabackend.domain.user.dto.req.UserSignUpRequestDto;
import com.example.moabackend.domain.user.dto.res.UserResponseDto;
import com.example.moabackend.domain.user.service.UserService;
import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.annotation.UserId;
import com.example.moabackend.global.code.GlobalSuccessCode;
import com.example.moabackend.global.security.dto.JwtDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원가입 및 사용자 관리", description = "단계별 회원가입, 역할 선택, 사용자 정보 관리 API")
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BaseResponse<Void> preSignUp(@Valid @RequestBody UserSignUpRequestDto request) {
        // 1단계: 기본 정보와 전화번호를 Redis에 임시 저장 (전화번호를 Redis 키로 사용)
        userService.preSignUp(request);
        return BaseResponse.success(UserSuccessCode.USER_REGISTER_TEMP_SAVED, null);
    }

    @PostMapping("/register/code-request")
    public BaseResponse<Void> requestSignUpSms(@Valid @RequestBody PhoneNumberRequestDto request) {
        // 2-1단계: 전화번호 중복 체크 후, 회원가입용 인증 코드를 발송
        userService.requestSignUpSms(request.phoneNumber());
        return BaseResponse.success(UserSuccessCode.AUTH_CODE_SENT, null);
    }

    @PostMapping("/register/code-complete")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<JwtDTO> confirmSignUp(
            @Valid @RequestBody SignUpConfirmationRequestDto request) {
        // 2-2단계: 인증 코드 검증 및 Redis 임시 데이터로 DB에 최종 사용자 생성, JWT 토큰 발행
        JwtDTO jwt = userService.confirmSignUpAndLogin(request.phoneNumber(), request.authCode());
        return BaseResponse.success(GlobalSuccessCode.CREATED, jwt);
    }

    @PostMapping("/register/select-role")
    public BaseResponse<UserResponseDto> selectUserRole(
            @UserId Long userId,
            @Valid @RequestBody UserRoleSelectionRequestDto request) {
        // 3단계: 인증된 사용자(userId)의 역할(PARENT/CHILD) 확정 및 부모-자녀 연결
        UserResponseDto response = userService.selectRoleAndLinkParent(userId, request.role(), request.parentCode());
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, response);
    }

    // 전화번호만 받는 DTO 정의 (클래스 분리도 가능)
    public record PhoneNumberRequest(
            @NotBlank(message = "전화번호는 필수 입력값입니다.")
            @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
            String phoneNumber
    ) {
    }
}