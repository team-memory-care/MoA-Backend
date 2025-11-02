package com.example.moabackend.domain.user.dto;

import com.example.moabackend.domain.user.entity.type.EUserGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserSignUpRequest(
        @NotBlank(message = "이름은 필수 입력값입니다.")
        String name,

        @NotBlank(message = "생년월일은 필수 입력값입니다.")
        @Pattern(regexp = "^\\d{8}$", message = "생년월일은 yyyyMMdd 형식의 8자리 숫자여야 합니다.")
        String birthDate,

        @NotBlank(message = "전화번호는 필수 입력값입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
        String phoneNumber,

        @NotNull EUserGender gender
) {
}