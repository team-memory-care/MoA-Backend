package com.example.moabackend.domain.notification.event;

import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.sse.dto.EMessageType;
import com.example.moabackend.domain.sse.service.SseEmitterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStreamConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final SseEmitterService sseService;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        List<MapRecord<String, Object, Object>> messages =
                redisTemplate.opsForStream().read(
                        Consumer.from(NOTI_GROUP, NOTI_CONSUMER),
                        StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
                        StreamOffset.create(NOTI_STREAM_KEY, ReadOffset.lastConsumed())
                );

        if (messages == null || messages.isEmpty()) return;

        for (MapRecord<String, Object, Object> msg : messages) {
            try {
                Object payloadRaw = msg.getValue().get(FIELD_PAYLOAD);
                if (payloadRaw == null) {
                    ack(msg);
                    continue;
                }

                NotificationPayload payload =
                        objectMapper.readValue(payloadRaw.toString(), NotificationPayload.class);

                sseService.sendToClient(payload.userId(), EMessageType.NOTIFICATION, payload);

                ack(msg);

            } catch (Exception e) {
                log.error("Notification consume error. id={}", msg.getId(), e);
            }
        }
    }

    private void ack(MapRecord<String, Object, Object> msg) {
        redisTemplate.opsForStream().acknowledge(NOTI_STREAM_KEY, NOTI_GROUP, msg.getId());
    }
}