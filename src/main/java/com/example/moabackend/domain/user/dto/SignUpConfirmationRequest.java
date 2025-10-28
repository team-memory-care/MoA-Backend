package com.example.moabackend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpConfirmationRequest(
        @NotBlank(message = "인증 코드는 필수 입력값입니다.")
        @Pattern(regexp = "^\\d{4}$", message = "인증 코드는 4자리 숫자여야 합니다.")
        String authCode
) {
}