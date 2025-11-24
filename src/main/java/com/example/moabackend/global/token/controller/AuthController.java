package com.example.moabackend.global.token.controller;

import com.example.moabackend.domain.user.code.UserSuccessCode;
import com.example.moabackend.domain.user.service.UserService;
import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.annotation.UserId;
import com.example.moabackend.global.code.GlobalSuccessCode;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.token.dto.req.LogoutRequestDto;
import com.example.moabackend.global.token.dto.req.ReissueTokenRequestDto;
import com.example.moabackend.global.token.dto.res.ReissueTokenResponseDto;
import com.example.moabackend.global.token.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 및 토큰 관리", description = "로그인, 토큰 발급, 부모 코드 관리 API")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * [로그인]
     * 전화번호와 인증 코드를 검증하고, 성공 시 Access/Refresh JWT 토큰을 발급하여 클라이언트에게 반환합니다.
     */
    @PostMapping("/login")
    public BaseResponse<JwtDTO> login(
            @RequestParam @NotNull(message = "전화번호는 필수입니다.") String phoneNumber,
            @RequestParam @NotNull(message = "인증 코드는 필수입니다.") String authCode) {
        JwtDTO jwt = authService.login(phoneNumber, authCode);
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, jwt);
    }

    /**
     * [로그인 인증 코드 발송]
     * 회원가입이 완료된 사용자가 로그인하기 위해 새로운 인증 코드 SMS를 요청할 때 사용됩니다.
     */

    @PostMapping("/sms/request")
    public BaseResponse<Void> requestLoginSms(
            @RequestParam @NotNull(message = "전화번호는 필수입니다.") String phoneNumber) {
        authService.generateAuthCode(phoneNumber);
        return BaseResponse.success(UserSuccessCode.AUTH_CODE_SENT, null);
    }

    /**
     * [부모 코드 발급/조회]
     * 로그인 및 인증이 완료된 부모 사용자가 자신의 4자리 회원 코드를 조회하거나, 코드가 없을 경우 새로 발급받을 때 사용됩니다.
     */
    @PostMapping("/code/issue")
    public BaseResponse<String> issueParentCode(@AuthenticationPrincipal Long userId) {
        String code = userService.issueOrGetParentCode(userId);
        return BaseResponse.success(GlobalSuccessCode.SUCCESS, code);
    }

    @PostMapping("/token/reissue")
    @Operation(summary = "refresh token 재발급", description = "refreshToken 재발급 API입니다.")
    public BaseResponse<ReissueTokenResponseDto> reissue(
            @RequestBody ReissueTokenRequestDto reissueTokenRequestDto
    ) {
        return BaseResponse.success(UserSuccessCode.REISSUE_SUCCESS, authService.reissueToken(reissueTokenRequestDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 API입니다.")
    public BaseResponse<Void> logout(
            @RequestBody LogoutRequestDto logoutRequestDto
    ) {
        authService.logout(logoutRequestDto);
        return BaseResponse.success(UserSuccessCode.LOGOUT_SUCCESS, null);
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원 탈퇴", description = "회원탈퇴 API입니다.")
    public BaseResponse<Void> withdraw(
            @UserId Long userId
    ) {
        authService.withdraw(userId);
        return BaseResponse.success(UserSuccessCode.USER_WITHDRAW_SUCCESS, null);
    }
}