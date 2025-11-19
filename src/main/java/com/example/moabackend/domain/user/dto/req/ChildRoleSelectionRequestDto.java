package com.example.moabackend.domain.user.dto.req;

import com.example.moabackend.domain.user.entity.type.ERole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChildRoleSelectionRequestDto(
        @Schema(description = "선택한 역할(CHILD로 고정)", example = "CHILD")
        ERole role,

        @NotBlank(message = "부모 회원 코드는 필수 입력 값입니다.")
        @Size(max = 4, message = "부모 회원코드는 4자리 숫자여야 합니다.")
        @Pattern(regexp = "^\\d{4}$", message = "부모 회원코드는 4자리 숫자여야 합니다.")
        @Schema(description = "부모 회원코드", example = "4321")
        String parentCode
) {
}
