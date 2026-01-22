package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.dto.req.UserRegisterRequestDto;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.EUserGender;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.token.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserServiceImpl userService;

    // --- [Scenario: Onboarding - SMS Request] ---

    @Test
    @DisplayName("회원가입 SMS 요청: 신규 번호인 경우 인증 코드 발송")
    void requestSignUpSms_NewUser_Success() {
        // [Logic] 중복 가입 여부 확인 후 신규 유저 대상 인증 코드 생성
        String phoneNumber = "01012345678";
        String expectedAuthCode = "123456";

        given(userRepository.findByPhoneNumber(anyString())).willReturn(Optional.empty());
        given(authService.generateSignUpAuthCode(phoneNumber)).willReturn(expectedAuthCode);

        String result = userService.requestSignUpSms(phoneNumber);

        assertThat(result).isEqualTo(expectedAuthCode);
    }

    @Test
    @DisplayName("회원가입 SMS 요청: 이미 ACTIVE 상태인 유저의 중복 가입 시도 차단")
    void requestSignUpSms_AlreadyExists_ThrowsException() {
        // [Validation] 활성 유저(ACTIVE) 존재 시 USER_ALREADY_EXISTS 예외 반환
        String phoneNumber = "01035477120";
        String resolvedNumber = "821035477120";

        User existingUser = User.builder()
                .phoneNumber(resolvedNumber)
                .status(EUserStatus.ACTIVE)
                .build();

        given(userRepository.findByPhoneNumber(resolvedNumber)).willReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.requestSignUpSms(phoneNumber))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_ALREADY_EXISTS);
    }

    // --- [Scenario: Onboarding - Verification & Signup] ---

    @Test
    @DisplayName("회원가입 완료: 인증 코드 불일치 시 INVALID_AUTH_CODE 예외 반환")
    void confirmSignUpAndLogin_InvalidAuthCode_ThrowsException() {
        // [Security] 제출된 인증 코드가 Redis 저장값과 다른 경우
        UserRegisterRequestDto request = createRegisterRequest("01012345678", "wrong-code");
        given(authService.verifyAuthCode(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> userService.confirmSignUpAndLogin(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_AUTH_CODE);
    }

    @Test
    @DisplayName("회원가입 완료: 신규 유저 - 계정 생성 및 영속화 후 토큰 발급")
    void confirmSignUpAndLogin_NewUser_Success() {
        // [Flow] 인증 성공 -> 유저 정보 저장 -> JWT 발급 시나리오
        UserRegisterRequestDto request = createRegisterRequest("01012345678", "123456");
        JwtDTO expectedTokens = JwtDTO.builder().accessToken("access-token").build();

        given(authService.verifyAuthCode(anyString(), anyString())).willReturn(true);
        given(userRepository.findByPhoneNumber(anyString())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(authService.generateTokensForUser(any(User.class))).willReturn(expectedTokens);

        JwtDTO result = userService.confirmSignUpAndLogin(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
        verify(userRepository, times(1)).save(any(User.class)); // 영속화 호출 여부 검증
    }

    @Test
    @DisplayName("회원가입 완료: 탈퇴 유저 - 기존 정보 활성화(Dirty Checking) 및 토큰 발급")
    void confirmSignUpAndLogin_WithdrawnUser_Success() {
        // [Business Rule] WITHDRAWN 유저는 신규 생성 없이 기존 레코드 재활용(ACTIVE 전환)
        UserRegisterRequestDto request = createRegisterRequest("01012345678", "123456");
        User withdrawnUser = User.builder().status(EUserStatus.WITHDRAWN).build();
        JwtDTO expectedTokens = JwtDTO.builder().accessToken("re-access-token").build();

        given(authService.verifyAuthCode(anyString(), anyString())).willReturn(true);
        given(userRepository.findByPhoneNumber(anyString())).willReturn(Optional.of(withdrawnUser));
        given(authService.generateTokensForUser(any(User.class))).willReturn(expectedTokens);

        JwtDTO result = userService.confirmSignUpAndLogin(request);

        assertThat(result.accessToken()).isEqualTo("re-access-token");
        assertThat(withdrawnUser.getStatus()).isEqualTo(EUserStatus.ACTIVE); // 상태값 변경 확인
        verify(userRepository, times(0)).save(any(User.class)); // 불필요한 Insert 방지 검증
    }

    // --- [Test Data Factory] ---

    private UserRegisterRequestDto createRegisterRequest(String phoneNumber, String authCode) {
        return new UserRegisterRequestDto(
                "테스터", "19950320", phoneNumber, EUserGender.MALE, authCode
        );
    }
}