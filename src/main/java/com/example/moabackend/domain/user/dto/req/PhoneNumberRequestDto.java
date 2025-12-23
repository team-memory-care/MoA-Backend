package com.example.moabackend.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneNumberRequestDto(
        @NotBlank(message = "전화번호는 필수 입력값입니다.")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "전화번호는 10~15자리의 숫자여야 합니다.")
        @Schema(description = "전화번호 (국가코드 포함, 예: 821012345678)", example = "821012345678")
        String phoneNumber
) {
}