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

                int retryCount = Integer.parseInt(msg.getValue().get(REPORT_RETRY_COUNT).toString());
                String payloadJson = msg.getValue().get(REPORT_MESSAGE_MAP_KEY).toString();

                ReportMessagePayload payload =
                        objectMapper.readValue(payloadJson, ReportMessagePayload.class);
                try {
                    reportFacade.processReport(payload);

                    redisTemplate.opsForStream()
                            .acknowledge(REPORT_STREAM_KEY, REPORT_GROUP, msg.getId());

                    log.info("Retry success: {}", msg.getId());
                } catch (OpenAiRateLimitException rateEx) {
                    long backoff = (long) Math.pow(2, retryCount) * 1000;
                    backoff = Math.min(backoff, 30000);

                    log.warn("RateLimit 429. backoff {}ms (retry={})", backoff, retryCount);

                    Thread.sleep(backoff);
                } catch (Exception ex) {
                    retryCount++;

                    if (retryCount >= 3) {
                        moveToDLQ(msg, retryCount);
                        redisTemplate.opsForStream()
                                .acknowledge(REPORT_STREAM_KEY, REPORT_GROUP, msg.getId());
                    } else {
                        redisTemplate.opsForHash().put(
                                STREAM_ENTRY_KEY(msg),
                                REPORT_RETRY_COUNT,
                                retryCount
                        );
                    }
                }
            } catch (Exception ex) {
                log.error("Unexpected error during retry", ex);
            }
        }
    }

    private String STREAM_ENTRY_KEY(MapRecord<String, Object, Object> msg) {
        return REPORT_STREAM_KEY + "-" + msg.getId();
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