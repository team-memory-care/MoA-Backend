package com.example.moabackend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpConfirmationRequest(
        @NotBlank(message = "전화번호는 필수 입력값입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
        String phoneNumber,

        @NotBlank(message = "인증 코드는 필수 입력값입니다.")
        @Pattern(regexp = "^\\d{4}$", message = "인증 코드는 4자리 숫자여야 합니다.")
        String authCode
) {
}