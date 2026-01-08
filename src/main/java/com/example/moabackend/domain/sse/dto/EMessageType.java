package com.example.moabackend.domain.sse.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum EMessageType {
    NOTIFICATION("notification"),
    CONNECT("connect");

    private final String eventName;
}
