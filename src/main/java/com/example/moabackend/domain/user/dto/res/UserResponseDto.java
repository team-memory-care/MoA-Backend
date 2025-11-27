package com.example.moabackend.domain.user.dto.res;

import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserGender;
import com.example.moabackend.domain.user.entity.type.EUserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record UserResponseDto(
        @Schema(description = "사용자 고유 ID", example = "1")
        Long id,

        @Schema(description = "사용자 이름", example = "김모아")
        String name,

        @Schema(description = "전화번호", example = "01012345678")
        String phoneNumber,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "생년월일", example = "1995-03-20")
        LocalDate birthDate,

        @Schema(description = "역할 (PARENT 또는 CHILD)", example = "PARENT")
        ERole role,

        @Schema(description = "성별 (MALE 또는 FEMALE)", example = "MALE")
        EUserGender gender,

        @Schema(description = "상태 (ACTIVE, INACTIVE 등)", example = "ACTIVE")
        EUserStatus status,

        @Schema(description = "부모 발급 연결 코드 (부모 사용자일 경우)", example = "1234")
        String parentCode,

        @Schema(description = "연결된 부모 사용자의 ID (자녀 사용자일 경우)", example = "20")
        List<Long> parentUserIds
) {

    public static UserResponseDto from(User user) {
        List<Long> parentIds = (user.getParents() == null) ?
                Collections.emptyList() : user.getParents().stream()
                .map(User::getId)
                .toList();
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getBirthDate(),
                user.getRole(),
                user.getGender(),
                user.getStatus(),
                user.getParentCode(),
                parentIds
        );
    }
}