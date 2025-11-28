package com.example.moabackend.domain.report.event;

import com.example.moabackend.domain.report.dto.req.ReportMessagePayload;
import com.example.moabackend.domain.report.service.report.ReportFacade;
import com.example.moabackend.global.exception.OpenAiRateLimitException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
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

        if (pending.isEmpty()) {
            return;
        }

        for (PendingMessage pm : pending) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        redisTemplate.opsForStream().read(
                                Consumer.from(REPORT_GROUP, REPORT_CONSUMER),
                                StreamReadOptions.empty(),
                                StreamOffset.create(REPORT_STREAM_KEY, ReadOffset.from(pm.getId()))
                        );

                if (records == null || records.isEmpty()) {
                    continue;
                }

                MapRecord<String, Object, Object> msg = records.get(0);

                int retryCount = Integer.parseInt(msg.getValue().get(REPORT_RETRY_COUNT).toString());
                String payloadJson = msg.getValue().get(REPORT_MESSAGE_MAP_KEY).toString();

                ReportMessagePayload payload =
                        objectMapper.readValue(payloadJson, ReportMessagePayload.class);

                try {
                    reportFacade.processReport(payload);

                    redisTemplate.opsForStream()
                            .acknowledge(REPORT_STREAM_KEY, REPORT_GROUP, msg.getId());

                    log.info("Retry success: {}", msg.getId());
                } catch (Exception ex) {
                    if (ex instanceof OpenAiRateLimitException) {
                        log.warn("429 Received on retry. Backoff and retry later...");
                        Thread.sleep(2000);
                        continue;
                    }

                    retryCount++;
                    log.warn("Retry failed: {} (count={})", msg.getId(), retryCount);

                    if (retryCount >= 3) {
                        moveToDLQ(msg, retryCount);

                        redisTemplate.opsForStream()
                                .acknowledge(REPORT_STREAM_KEY, REPORT_GROUP, msg.getId());

                    } else {
                        redisTemplate.opsForStream().add(
                                StreamRecords.newRecord()
                                        .ofMap(Map.of(
                                                REPORT_MESSAGE_MAP_KEY, payloadJson,
                                                REPORT_RETRY_COUNT, String.valueOf(retryCount)
                                        ))
                                        .withStreamKey(REPORT_STREAM_KEY)
                        );

                        redisTemplate.opsForStream()
                                .acknowledge(REPORT_STREAM_KEY, REPORT_GROUP, msg.getId());
                    }
                }

            } catch (Exception e) {
                log.error("Unexpected retry error", e);
            }
        }
    }

    private void moveToDLQ(MapRecord<String, Object, Object> msg, int retryCount) {

        redisTemplate.opsForStream().add(
                StreamRecords.newRecord()
                        .ofMap(Map.of(
                                REPORT_MESSAGE_MAP_KEY,
                                msg.getValue().get(REPORT_MESSAGE_MAP_KEY),
                                REPORT_RETRY_COUNT, retryCount
                        ))
                        .withStreamKey(REPORT_DLQ_STREAM_KEY)
        );

        log.error("Moved to DLQ: {}", msg.getId());
    }
}