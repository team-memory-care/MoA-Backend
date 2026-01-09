package com.example.moabackend.domain.notification.event;

import com.example.moabackend.domain.notification.converter.NotificationConverter;
import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.notification.entity.Notification;
import com.example.moabackend.domain.notification.repository.NotificationRepository;
import com.example.moabackend.domain.notification.service.NotificationService;
import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.report.repository.ReportRepository;
import com.example.moabackend.domain.sse.dto.EMessageType;
import com.example.moabackend.domain.sse.service.SseEmitterService;
import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStreamRetryConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void retryPending() {
        PendingMessages pending =
                redisTemplate.opsForStream().pending(
                        NOTI_STREAM_KEY,
                        NOTI_GROUP,
                        Range.unbounded(),
                        20
                );

        if (pending == null || pending.isEmpty()) return;

        for (PendingMessage pm : pending) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        redisTemplate.opsForStream().read(
                                Consumer.from(NOTI_GROUP, NOTI_CONSUMER),
                                StreamReadOptions.empty().count(1).block(Duration.ofSeconds(1)),
                                StreamOffset.create(NOTI_STREAM_KEY, ReadOffset.from(pm.getId()))
                        );

                if (records == null || records.isEmpty()) continue;

                MapRecord<String, Object, Object> msg = records.get(0);

                int retryCount = parseInt(msg.getValue().get(FIELD_RETRY), 0);
                String payloadJson = String.valueOf(msg.getValue().get(FIELD_PAYLOAD));

                NotificationPayload payload =
                        objectMapper.readValue(payloadJson, NotificationPayload.class);

                try {
                    notificationService.processNotification(payload);

                    ack(msg);
                    log.info("Retry success. id={}", msg.getId());

                } catch (Exception ex) {
                    if (isRateLimit(ex)) {
                        long backoffMs = calcBackoffMs(retryCount);
                        log.warn("Rate limited. backoffMs={}, id={}", backoffMs, msg.getId());
                        Thread.sleep(backoffMs);
                        continue;
                    }

                    retryCount++;
                    if (retryCount >= 3) {
                        moveToDLQ(payloadJson, retryCount);
                        ack(msg);
                        log.error("Moved to DLQ. id={}", msg.getId());
                    } else {
                        requeue(payloadJson, retryCount);
                        ack(msg);
                        log.warn("Requeued. id={}, retryCount={}", msg.getId(), retryCount);
                    }
                }

            } catch (Exception e) {
                log.error("Retry pending error", e);
            }
        }
    }

    private void requeue(String payloadJson, int retryCount) {
        redisTemplate.opsForStream().add(
                StreamRecords.newRecord()
                        .ofMap(Map.of(
                                FIELD_PAYLOAD, payloadJson,
                                FIELD_RETRY, String.valueOf(retryCount)
                        ))
                        .withStreamKey(NOTI_STREAM_KEY),
                RedisStreamCommands.XAddOptions.maxlen(MAX_LEN).approximateTrimming(true)
        );
    }

    private void moveToDLQ(String payloadJson, int retryCount) {
        redisTemplate.opsForStream().add(
                StreamRecords.newRecord()
                        .ofMap(Map.of(
                                FIELD_PAYLOAD, payloadJson,
                                FIELD_RETRY, String.valueOf(retryCount)
                        ))
                        .withStreamKey(NOTI_DLQ_STREAM_KEY),
                RedisStreamCommands.XAddOptions.maxlen(MAX_LEN).approximateTrimming(true)
        );
    }

    private void ack(MapRecord<String, Object, Object> msg) {
        redisTemplate.opsForStream().acknowledge(NOTI_STREAM_KEY, NOTI_GROUP, msg.getId());
    }

    private int parseInt(Object v, int defaultVal) {
        try {
            if (v == null) return defaultVal;
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private boolean isRateLimit(Exception ex) {
        return ex.getMessage() != null && ex.getMessage().contains("429");
    }

    private long calcBackoffMs(int retryCount) {
        long ms = (long) Math.pow(2, Math.max(retryCount, 1)) * 1000L;
        return Math.min(ms, 30000L);
    }
}