package com.example.moabackend.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpConfirmationRequestDto(
        @NotBlank(message = "전화번호는 필수 입력값입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
        @Schema(description = "전화번호", example = "01012345678")
        String phoneNumber,

        @NotBlank(message = "인증 코드는 필수 입력값입니다.")
        @Pattern(regexp = "^\\d{4}$", message = "인증 코드는 4자리 숫자여야 합니다.")
        @Schema(description = "4자리 인증 코드", example = "1234")
        String authCode
) {
}