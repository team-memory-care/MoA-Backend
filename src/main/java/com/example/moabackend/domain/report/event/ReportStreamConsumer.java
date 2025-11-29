package com.example.moabackend.domain.report.event;

import com.example.moabackend.domain.report.dto.req.ReportMessagePayload;
import com.example.moabackend.domain.report.service.report.ReportFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportStreamConsumer {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ReportFacade reportFacade;

    @Scheduled(fixedDelay = 2000)
    public void consume() {

        List<MapRecord<String, Object, Object>> messages =
                redisTemplate.opsForStream().read(
                        Consumer.from(REPORT_GROUP, REPORT_CONSUMER),
                        StreamReadOptions.empty().count(5),
                        StreamOffset.create(REPORT_STREAM_KEY, ReadOffset.lastConsumed())
                );

        if (messages == null || messages.isEmpty()) return;

        for (MapRecord<String, Object, Object> msg : messages) {

            try {
                String payload = msg.getValue().get(REPORT_MESSAGE_MAP_KEY).toString();
                ReportMessagePayload message =
                        objectMapper.readValue(payload, ReportMessagePayload.class);

                reportFacade.processReport(message);

                redisTemplate.opsForStream()
                        .acknowledge(REPORT_STREAM_KEY, REPORT_GROUP, msg.getId());

            } catch (Exception e) {
                // 실패한 메시지는 ack하지 않는다 → pending → retry 가능
                log.error("Error processing message id=" + msg.getId(), e);
            }
        }
    }
}
