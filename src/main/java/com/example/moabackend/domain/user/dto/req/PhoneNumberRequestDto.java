package com.example.moabackend.domain.user.dto.req;

import com.example.moabackend.global.constant.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneNumberRequestDto(
        @NotBlank(message = "전화번호는 필수 입력값입니다.")
        @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
        @Schema(description = "전화번호 (국가코드 포함, 예: 821012345678)", example = "821012345678")
        String phoneNumber
) {
}