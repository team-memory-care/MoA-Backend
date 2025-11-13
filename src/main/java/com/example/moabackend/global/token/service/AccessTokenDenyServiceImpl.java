package com.example.moabackend.global.token.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.example.moabackend.global.constant.Constants.ACCESS_TOKEN_DENY;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenDenyServiceImpl implements AccessTokenDenyService {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void deny(String jti, Duration ttl) {
        if (jti == null || jti.isBlank()) return;
        if (ttl == null || ttl.isZero() || ttl.isNegative()) return;
        stringRedisTemplate.opsForValue().set(ACCESS_TOKEN_DENY + jti, "1", ttl);
    }

    @Override
    public boolean isDenied(String jti) {
        if (jti == null || jti.isBlank()) return false;
        Boolean exists = stringRedisTemplate.hasKey(ACCESS_TOKEN_DENY + jti);
        return Boolean.TRUE.equals(exists);
    }
}