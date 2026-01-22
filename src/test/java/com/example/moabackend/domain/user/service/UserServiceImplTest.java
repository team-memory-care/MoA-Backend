package com.example.moabackend.domain.user.service;

import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
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
}
