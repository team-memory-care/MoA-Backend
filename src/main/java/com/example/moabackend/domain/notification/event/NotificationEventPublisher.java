package com.example.moabackend.domain.notification.event;

import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.report.entity.type.EReportType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.Map;

import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishAfterCommit(Long userId, EReportType reportType, LocalDate reportDate) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            publishNow(userId, reportType, reportDate);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publishNow(userId, reportType, reportDate);
            }
        });
    }

    private void publishNow(Long userId, EReportType reportType, LocalDate reportDate) {
        try {
            String json = objectMapper.writeValueAsString(
                    new NotificationPayload(userId, reportType, reportDate)
            );

            redisTemplate.opsForStream().add(
                    StreamRecords.newRecord()
                            .ofMap(Map.of(
                                    FIELD_PAYLOAD, json,
                                    FIELD_RETRY, "0"
                            ))
                            .withStreamKey(NOTI_STREAM_KEY),
                    RedisStreamCommands.XAddOptions.maxlen(MAX_LEN).approximateTrimming(true)
            );

            log.info("Published notification event. userId={}, type={}, date={}", userId, reportType, reportDate);

        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
        }
    }
}