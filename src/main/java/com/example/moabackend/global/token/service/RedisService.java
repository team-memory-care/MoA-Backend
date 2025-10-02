package com.example.moabackend.global.token.service;

import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void setData(String key, Object value, long ttlMinutes) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(ttlMinutes));
        } catch (JsonProcessingException e) {
            throw new CustomException(GlobalErrorCode.REDIS_ERROR);
        }
    }

    public <T> T getData(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new CustomException((GlobalErrorCode.REDIS_ERROR));
        }
    }

    public void deleteData(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new CustomException(GlobalErrorCode.REDIS_ERROR);
        }
    }
}
