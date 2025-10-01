package com.example.moabackend.domain.user.dto;

import com.example.moabackend.domain.user.entity.type.ERole;
import com.example.moabackend.domain.user.entity.type.EUserGender;

public record UserSignUpRequest(
        String name,
        String phoneNumber,
        String birthDate,
        EUserGender gender,
        ERole role
) {
}
