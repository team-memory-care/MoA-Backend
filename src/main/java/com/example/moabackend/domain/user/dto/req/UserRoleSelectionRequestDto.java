package com.example.moabackend.domain.user.dto.req;

import com.example.moabackend.domain.user.entity.type.ERole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRoleSelectionRequestDto(
        @NotNull(message = "역할 선택은 필수입니다.")
        ERole role,

        @Size(max = 4, message = "부모 회원코드는 4자리 숫자여야 합니다.")
        @Pattern(regexp = "^\\d{4}$", message = "부모 회원코드는 4자리 숫자여야 합니다.")
        String parentCode
) {
}
