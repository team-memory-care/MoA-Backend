package com.example.moabackend.global.config;

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
public class ReportStreamInitializer {
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        try {
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(REPORT_STREAM_KEY))) {
                redisTemplate.opsForStream().add(
                        StreamRecords.newRecord()
                                .ofMap(Map.of("seed", "init"))
                                .withStreamKey(REPORT_STREAM_KEY),
                        RedisStreamCommands.XAddOptions
                                .maxlen(REDIS_STREAM_MAX_LEN)
                                .approximateTrimming(true)
                );
                log.info("Stream '{}' created with seed record", REPORT_STREAM_KEY);
            }

            try {
                redisTemplate.opsForStream().createGroup(
                        REPORT_STREAM_KEY,
                        REPORT_GROUP
                );
                log.info("Consumer group '{}' created", REPORT_GROUP);

            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("BUSYGROUP")) {
                    log.info("Consumer group '{}' already exists", REPORT_GROUP);
                } else {
                    throw e;
                }
            }

        } catch (Exception e) {
            log.error("Failed to initialize Redis Stream", e);
        }
    }
}
