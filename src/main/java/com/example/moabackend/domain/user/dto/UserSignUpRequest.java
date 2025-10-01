package com.example.moabackend.domain.user.dto;

import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserGender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record UserSignUpRequest(
        @NotBlank(message = "이름은 필수 입력값입니다.")
        String name,

        @NotBlank(message = "전화번호는 필수 입력값입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
        String phoneNumber,

        @NotBlank(message = "생년월일은 필수 입력값입니다.")
        @Pattern(
                regexp = "^\\d{8}$",
                message = "생년월일은 yyyyMMdd 형식의 8자리 숫자여야 합니다."
        )
        String birthDate,
        EUserGender gender,
        ERole role
) {
}
