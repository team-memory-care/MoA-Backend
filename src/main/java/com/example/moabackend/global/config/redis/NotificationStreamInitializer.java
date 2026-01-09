package com.example.moabackend.global.config.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStreamInitializer {
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        try {
            ensureStreamExists();
            createConsumerGroup();
        } catch (Exception e) {
            log.error("CRITICAL: Failed to initialize Redis Stream infrastructure", e);
        }
    }

    private void ensureStreamExists() {
        if (!redisTemplate.hasKey(NOTI_STREAM_KEY)) {
            redisTemplate.opsForStream().add(
                    StreamRecords.newRecord()
                            .ofMap(Map.of("seed", "init"))
                            .withStreamKey(NOTI_STREAM_KEY),
                    RedisStreamCommands.XAddOptions.maxlen(REDIS_STREAM_MAX_LEN).approximateTrimming(true)
            );
            log.info("Stream '{}' created with seed record", NOTI_STREAM_KEY);
        }
    }

    private void createConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(NOTI_STREAM_KEY, NOTI_GROUP);
            log.info("Consumer group '{}' created for stream '{}'", NOTI_GROUP, NOTI_STREAM_KEY);
        } catch (org.springframework.data.redis.RedisSystemException e) {
            if (e.getCause() instanceof io.lettuce.core.RedisBusyException) {
                log.info("Consumer group '{}' already exists. Skipping creation.", NOTI_GROUP);
            } else {
                throw e;
            }
        }
    }
}