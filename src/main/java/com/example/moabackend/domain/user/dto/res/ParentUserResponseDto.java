package com.example.moabackend.domain.user.dto.res;


import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserGender;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ParentUserResponseDto(
        @Schema(description = "사용자 고유 ID", example = "1")
        Long id,

        @Schema(description = "사용자 아름", example = "김모아")
        String name,

        @Schema(description = "전화번호", example = "01012345678")
        String phoneNumber,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "생년월일", example = "1995-03-20")
        LocalDate birthdate,

        @Schema(description = "역할", example = "PARENT")
        ERole role,

        @Schema(description = "성별(MALE 또는 FEMALE)", example = "MALE")
        EUserGender gender,

        @Schema(description = "상태 (ACTIVE, INACTIVE 등)", example = "ACTIVE")
        EUserStatus status,

        @Schema(description = "본인이 발급받은 회원 코드", example = "1234")
        String parentCode
) {

    public static ParentUserResponseDto from(User user) {
        return new ParentUserResponseDto(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getBirthDate(),
                user.getRole(),
                user.getGender(),
                user.getStatus(),
                user.getParentCode()
        );
    }
}