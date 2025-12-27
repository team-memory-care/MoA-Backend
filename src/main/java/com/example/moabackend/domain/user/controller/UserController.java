package com.example.moabackend.domain.user.controller;

import com.example.moabackend.domain.user.code.UserSuccessCode;
import com.example.moabackend.domain.user.dto.req.ChildRoleSelectionRequestDto;
import com.example.moabackend.domain.user.dto.req.PhoneNumberRequestDto;
import com.example.moabackend.domain.user.dto.req.UserRegisterRequestDto;
import com.example.moabackend.domain.user.dto.res.ChildUserResponseDto;
import com.example.moabackend.domain.user.dto.res.ParentUserResponseDto;
import com.example.moabackend.domain.user.dto.res.UserResponseDto;
import com.example.moabackend.domain.user.service.UserService;
import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.annotation.UserId;
import com.example.moabackend.global.code.GlobalSuccessCode;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.token.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "사용자(User)", description = "회원가입, 역할 선택, 정보 관리 및 탈퇴 API")
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // --- [회원가입 관련] ---

    @Operation(summary = "회원가입 인증번호 전송", description = "회원가입을 위해 휴대폰 번호로 인증번호를 발송합니다.")
    @PostMapping("/signup/sms")
    public BaseResponse<Void> requestSignUpSms(@Valid @RequestBody PhoneNumberRequestDto request) {
        userService.requestSignUpSms(request.phoneNumber());
        return BaseResponse.success(UserSuccessCode.AUTH_CODE_SENT, null);
    }

    @Operation(summary = "회원가입 완료", description = "인증번호와 가입 정보를 제출하여 회원을 생성하고 토큰을 발급합니다.")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<JwtDTO> registerUser(
            @Valid @RequestBody UserRegisterRequestDto request) {
        JwtDTO jwt = userService.confirmSignUpAndLogin(request);
        return BaseResponse.success(GlobalSuccessCode.CREATED, jwt);
    }

    @Operation(summary = "부모 역할 선택", description = "가입 후 역할을 '부모'로 확정하고 고유 코드를 발급받습니다.")
    @PostMapping("/role/parent")
    public BaseResponse<ParentUserResponseDto> selectParentRole(@UserId Long userId) {
        ParentUserResponseDto response = userService.selectParentRole(userId);
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, response);
    }

    @Operation(summary = "자녀 역할 선택", description = "가입 후 역할을 '자녀'로 확정하고 부모와 연결합니다.")
    @PostMapping("/role/child")
    public BaseResponse<ChildUserResponseDto> selectChildRole(
            @UserId Long userId,
            @Valid @RequestBody ChildRoleSelectionRequestDto request) {
        ChildUserResponseDto response = userService.selectChildRoleAndLinkParent(userId, request.parentCode());
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, response);
    }

    // --- [사용자 정보 및 상태 관리] ---

    @Operation(summary = "부모 코드 발급", description = "부모 회원의 고유 코드를 새로 발급합니다.")
    @PostMapping("/parent-code/issue")
    public BaseResponse<String> issueNewParentCode(@UserId Long userId) {
        String code = userService.issueParentCode(userId);
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, code);
    }

    @Operation(summary = "부모 코드 조회", description = "발급된 부모 회원의 고유 코드를 조회합니다.")
    @GetMapping("/parent-code")
    public BaseResponse<String> getParentCode(@UserId Long userId) {
        String code = userService.getParentCode(userId);
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, code);
    }

    @Operation(summary = "회원 탈퇴", description = "회원 정보를 삭제(비활성화)하고 토큰을 만료시킵니다.")
    @DeleteMapping("/withdraw")
    public BaseResponse<Void> withdraw(@UserId Long userId) {
        authService.withdraw(userId);
        return BaseResponse.success(UserSuccessCode.USER_WITHDRAW_SUCCESS, null);
    }

    @GetMapping("/me")
    public BaseResponse<UserResponseDto> getUserInfo(
            @UserId Long userId) {
        UserResponseDto response = userService.findUserById(userId);
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, response);
    }

    @Operation(summary = "연결된 부모 목록 조회", description = "자녀 계정으로 연결된 부모들의 정보를 조회합니다.")
    @GetMapping("/my-parents")
    public BaseResponse<List<ChildUserResponseDto.LinkedParentResponseDto>> getMyParents(@UserId Long userId) {
        List<ChildUserResponseDto.LinkedParentResponseDto> response = userService.getMyParents(userId);
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, response);
    }
}