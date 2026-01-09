package com.example.moabackend.domain.notification.event;

import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStreamConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 1000)
    @Transactional
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

                notificationService.processNotification(payload);

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