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
import java.util.stream.Collectors;

public record ChildUserResponseDto(
        @Schema(description = "사용자 고유 ID", example = "1")
        Long id,

        @Schema(description = "사용자 이름", example = "김모아")
        String name,

        @Schema(description = "전화번호", example = "01012345678")
        String phoneNumber,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "생년월일", example = "1995-03-20")
        LocalDate birthDate,

        @Schema(description = "역할", example = "CHILD")
        ERole role,

        @Schema(description = "성별(MALE 또는 FEMALE)", example = "MALE")
        EUserGender gender,

        @Schema(description = "상태 (ACTIVE, INACTIVE 등)", example = "ACTIVE")
        EUserStatus status,

        @Schema(description = "연결된 부모 사용자의 ID", example = "20")
        List<LinkedParentResponseDto> parents
) {

    public static ChildUserResponseDto from(User user) {
        List<LinkedParentResponseDto> parentDtos = (user.getParents() == null) ?
                Collections.emptyList() :
                user.getParents().stream()
                        .map(LinkedParentResponseDto::from)
                        .collect(Collectors.toList());

        return new ChildUserResponseDto(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getBirthDate(),
                user.getRole(),
                user.getGender(),
                user.getStatus(),
                parentDtos
        );
    }

    public record LinkedParentResponseDto(
            @Schema(description = "부모 ID", example = "10")
            Long id,

            @Schema(description = "부모 이름", example = "이부모")
            String name,

            @JsonFormat(pattern = "yyyy-MM-dd")
            @Schema(description = "부모 생년월일", example = "1960-01-01")
            LocalDate birthDate,

            @Schema(description = "부모 성별", example = "FEMALE")
            EUserGender gender,

            @Schema(description = "부모 전화번호", example = "01098765432")
            String phoneNumber
    ) {
        public static LinkedParentResponseDto from(User parent) {
            return new LinkedParentResponseDto(
                    parent.getId(),
                    parent.getName(),
                    parent.getBirthDate(),
                    parent.getGender(),
                    parent.getPhoneNumber()
            );
        }
    }
}

