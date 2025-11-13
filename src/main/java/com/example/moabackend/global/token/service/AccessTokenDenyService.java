package com.example.moabackend.global.token.service;

import java.time.Duration;

public interface AccessTokenDenyService {
    void deny(String jti, Duration ttl); // 남은 유효시간만큼 차단
    boolean isDenied(String jti);
}
