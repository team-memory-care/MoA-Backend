package com.example.moabackend.domain.user.dto;

import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSignUpRequest(
        @NotBlank(message = "이름은 필수 입력값입니다.")
        String name,

        @NotBlank(message = "생년월일은 필수 입력값입니다.")
        @Pattern(regexp = "^\\d{8}$", message = "생년월일은 yyyyMMdd 형식의 8자리 숫자여야 합니다.")
        String birthDate,

        @NotNull EUserGender gender
) {
}
