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

    @Test
    @DisplayName("회원가입 SMS 요청 - 신규 번호일때 성공")
    void requestSignUpSms_NewUser_Success() {

        // given: 상황 준비
        String phoneNumber = "01012345678";
        String expectedAuthCode = "123456";

        given(userRepository.findByPhoneNumber(anyString())).willReturn(Optional.empty());
        given(authService.generateSignUpAuthCode(phoneNumber)).willReturn(expectedAuthCode);

        // when: 로직 실행
        String result = userService.requestSignUpSms(phoneNumber);

        // then: 결과 검증
        assertThat(result).isEqualTo(expectedAuthCode);
    }

    @Test
    @DisplayName("회원가입 SMS 요청 - 이미 활성화된 유저일 때 에러 발생")
    void requestSignUpSms_AlreadyExists_ThrowsException() {
        // given: 상황 준비
        String phoneNumber = "01035477120";
        String resolvedNumber = "821035477120";

        User existingUser = User.builder()
                .phoneNumber(resolvedNumber)
                .status(EUserStatus.ACTIVE)
                .build();

        given(userRepository.findByPhoneNumber(resolvedNumber)).willReturn(Optional.of(existingUser));

        // when: 로직 실행 & then: 결과 검증
        assertThatThrownBy(() -> userService.requestSignUpSms(phoneNumber))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("회원가입 완료 - 인증 번호가 일치하지 않으면 예외 발생")
    void confirmSignUpAndLogin_InvalidAuthCode_ThrowsException() {
        // given: 상황 준비
        UserRegisterRequestDto request = createRegisterRequest("01012345678", "wrong-code");
        // 인증 서비스가 false를 반환하도록 설정
        given(authService.verifyAuthCode(anyString(), anyString())).willReturn(false);

        // when: 로직 실행 & then: 결과 검증
        assertThatThrownBy(() -> userService.confirmSignUpAndLogin(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.INVALID_AUTH_CODE);
    }

    @Test
    @DisplayName("회원가입 완료 - 신규 유저일 경우 DB 저장 후 토큰을 반환한다")
    void confirmSignUpAndLogin_NewUser_Success() {
        // given: 상황 준비
        UserRegisterRequestDto request = createRegisterRequest("01012345678", "123456");
        JwtDTO expectedTokens = JwtDTO.builder().accessToken("access-token").build();

        given(authService.verifyAuthCode(anyString(), anyString())).willReturn(true);
        given(userRepository.findByPhoneNumber(anyString())).willReturn(Optional.empty());

        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(authService.generateTokensForUser(any(User.class))).willReturn(expectedTokens);

        // when: 로직 실행
        JwtDTO result = userService.confirmSignUpAndLogin(request);

        // then: 결과 검증
        assertThat(result.accessToken()).isEqualTo("access-token");
        verify(userRepository, times(1)).save(any(User.class));
    }

    private UserRegisterRequestDto createRegisterRequest(String phoneNumber, String authCode) {
        return new UserRegisterRequestDto(
                "테스터",
                "19950320",
                phoneNumber,
                EUserGender.MALE,
                authCode
        );
    }
}

