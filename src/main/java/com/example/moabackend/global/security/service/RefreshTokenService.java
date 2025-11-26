package com.example.moabackend.global.security.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.example.moabackend.global.constant.RedisKey.REFRESH_TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${jwt.refresh-token.expiration}")
    @Getter
    private Long refreshExpiration;

    private final StringRedisTemplate stringRedisTemplate;

    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(
                key,
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );
    }

    public void updateRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        log.info("[Redis] Update refreshToken key={} value={}", key, refreshToken);

        String existingToken = stringRedisTemplate.opsForValue().get(key);
        log.info("[Redis] Existing refreshToken for {} = {}", userId, existingToken);

        if (existingToken != null) {
            deleteRefreshToken(userId);
            log.info("[Redis] Deleted old refreshToken for {}", userId);
        }

        saveRefreshToken(userId, refreshToken);
        log.info("[Redis] Saved new refreshToken for {} = {}", userId, refreshToken);
    }

    public String getRefreshToken(Long userId){
        String key = REFRESH_TOKEN_PREFIX + userId;
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(Long userId){
        String key = REFRESH_TOKEN_PREFIX + userId;
        stringRedisTemplate.delete(key);
    }

    public boolean validateRefreshToken(Long userId, String refreshToken){
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

}
