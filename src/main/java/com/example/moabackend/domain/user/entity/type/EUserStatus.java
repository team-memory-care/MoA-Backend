package com.example.moabackend.domain.user.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EUserStatus {

    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;
}
