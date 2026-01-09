package com.example.moabackend.domain.report.event;

import com.example.moabackend.domain.report.dto.req.ReportMessagePayload;
import com.example.moabackend.domain.report.service.report.ReportFacade;
import com.example.moabackend.global.exception.OpenAiRateLimitException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportStreamRetryConsumer {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ReportFacade reportFacade;

    @Scheduled(fixedDelay = 5000)
    public void retryPending() {
        PendingMessages pending =
                redisTemplate.opsForStream().pending(
                        REPORT_STREAM_KEY,
                        REPORT_GROUP,
                        Range.unbounded(),
                        10
                );

        if (pending.isEmpty()) return;

        for (PendingMessage pm : pending) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        redisTemplate.opsForStream().read(
                                Consumer.from(REPORT_GROUP, REPORT_CONSUMER),
                                StreamReadOptions.empty(),
                                StreamOffset.create(REPORT_STREAM_KEY, ReadOffset.from(pm.getId()))
                        );

                if (records == null || records.isEmpty()) continue;

                MapRecord<String, Object, Object> msg = records.get(0);

                int retryCount = parseInt(msg.getValue().get(REPORT_RETRY_COUNT), 0);
                String payloadJson = String.valueOf(msg.getValue().get(REPORT_MESSAGE_MAP_KEY));

                ReportMessagePayload payload =
                        objectMapper.readValue(payloadJson, ReportMessagePayload.class);

                try {
                    reportFacade.processReport(payload);
                    ack(msg);
                    log.info("Report retry success. id={}", msg.getId());

                } catch (OpenAiRateLimitException rateEx) {
                    long backoffMs = calcBackoffMs(retryCount);
                    log.warn("OpenAI 429. backoff={}ms id={}", backoffMs, msg.getId());
                    Thread.sleep(backoffMs);
                } catch (Exception ex) {
                    retryCount++;
                    log.warn("Report retry failed. id={} retryCount={}", msg.getId(), retryCount);

                    if (retryCount >= 3) {
                        moveToDLQ(payloadJson, retryCount);
                        ack(msg);
                        log.error("Moved to Report DLQ. id={}", msg.getId());
                    } else {
                        requeue(payloadJson, retryCount);
                        ack(msg);
                    }
                }
            } catch (Exception e) {
                log.error("Unexpected report retry error", e);
            }
        }
    }


    private void requeue(String payloadJson, int retryCount) {
        redisTemplate.opsForStream().add(
                StreamRecords.newRecord()
                        .ofMap(Map.of(
                                REPORT_MESSAGE_MAP_KEY, payloadJson,
                                REPORT_RETRY_COUNT, String.valueOf(retryCount)
                        ))
                        .withStreamKey(REPORT_STREAM_KEY),
                RedisStreamCommands.XAddOptions.maxlen(REDIS_STREAM_MAX_LEN).approximateTrimming(true)
        );
    }

    private void moveToDLQ(String payloadJson, int retryCount) {
        redisTemplate.opsForStream().add(
                StreamRecords.newRecord()
                        .ofMap(Map.of(
                                REPORT_MESSAGE_MAP_KEY, payloadJson,
                                REPORT_RETRY_COUNT, String.valueOf(retryCount)
                        ))
                        .withStreamKey(REPORT_DLQ_STREAM_KEY),
                RedisStreamCommands.XAddOptions.maxlen(REDIS_STREAM_MAX_LEN).approximateTrimming(true)
        );
    }

    private void ack(MapRecord<String, Object, Object> msg) {
        redisTemplate.opsForStream()
                .acknowledge(REPORT_STREAM_KEY, REPORT_GROUP, msg.getId());
    }

    private int parseInt(Object v, int defaultVal) {
        try {
            if (v == null) return defaultVal;
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private long calcBackoffMs(int retryCount) {
        long ms = (long) Math.pow(2, Math.max(retryCount, 1)) * 1000L;
        return Math.min(ms, 30_000L);
    }
}