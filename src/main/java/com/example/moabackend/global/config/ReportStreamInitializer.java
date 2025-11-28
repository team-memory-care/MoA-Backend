package com.example.moabackend.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportStreamInitializer {
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        try {
            // Stream key가 없다면 최소 1개 추가
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(REPORT_STREAM_KEY))) {
                redisTemplate.opsForStream().add(
                        REPORT_STREAM_KEY,
                        Map.of("init", "1"),
                        RedisStreamCommands.XAddOptions.maxlen(REDIS_STREAM_MAX_LEN).approximateTrimming(true)
                );
                log.info("Stream 'report-stream' created");
            }

            // Consumer Group 생성
            try {
                redisTemplate.opsForStream().createGroup(
                        REPORT_STREAM_KEY,
                        REPORT_GROUP
                );
                log.info("Consumer group 'report-group' created");
            } catch (Exception e) {
                log.info("Consumer group already exists");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Redis Stream", e);
        }
    }
}
