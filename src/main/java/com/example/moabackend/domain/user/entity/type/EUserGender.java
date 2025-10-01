package com.example.moabackend.domain.user.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EUserGender {

    MALE("male"),
    FEMALE("female");

    private final String value;
}
