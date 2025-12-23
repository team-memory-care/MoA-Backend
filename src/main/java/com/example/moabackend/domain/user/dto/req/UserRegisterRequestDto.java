package com.example.moabackend.domain.user.dto.req;

import com.example.moabackend.domain.user.entity.type.EUserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserRegisterRequestDto(
        @NotBlank(message = "이름은 필수 입력값입니다.")
        @Schema(description = "사용자 이름", example = "김유저")
        String name,

        @NotBlank(message = "생년월일은 필수 입력값입니다.")
        @Pattern(regexp = "^\\d{8}$", message = "생년월일은 yyyyMMdd 형식의 8자리 숫자여야 합니다.")
        @Schema(description = "생년월일", example = "19950320")
        String birthDate,

        @NotBlank(message = "전화번호는 필수 입력값입니다.")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "전화번호는 10~15자리의 숫자여야 합니다 (국가코드 포함 권장).")
        @Schema(description = "전화번호 (국가코드 포함, 예: 821012345678)", example = "821012345678")
        String phoneNumber,

        @NotNull
        @Schema(description = "성별 (MALE 또는 FEMALE)", example = "FEMALE")
        EUserGender gender,

        @NotBlank(message = "인증 코드는 필수 입력값입니다.")
        @Pattern(regexp = "^\\d{4}$", message = "인증 코드는 4자리 숫자여야 합니다.")
        @Schema(description = "4자리 인증 코드", example = "1234")
        String authCode
) {
}
