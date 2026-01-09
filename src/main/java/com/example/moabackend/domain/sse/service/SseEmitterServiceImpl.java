package com.example.moabackend.domain.sse.service;

import com.example.moabackend.domain.sse.dto.EMessageType;
import com.example.moabackend.domain.sse.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterServiceImpl implements SseEmitterService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간
    private final SseEmitterRepository sseEmitterRepository;

    @Override
    public SseEmitter subscribe(Long userId, String lastEventId) {
        String emitterId = makeTimeIncludeId(userId);
        SseEmitter emitter = sseEmitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> sseEmitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> sseEmitterRepository.deleteById(emitterId));

        String eventId = makeTimeIncludeId(userId);
        sendNotification(emitter, eventId, emitterId, EMessageType.CONNECT, "connected!");

        if (!lastEventId.isEmpty()) {
            Map<String, Object> events = sseEmitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId));
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, EMessageType.NOTIFICATION, entry.getValue()));
        }

        return emitter;
    }

    @Override
    public void sendToClient(Long userId, EMessageType eventName, Object data) {
        String eventId = makeTimeIncludeId(userId);
        sseEmitterRepository.saveEventCache(eventId, data);

        Map<String, SseEmitter> emitters = sseEmitterRepository.findAllByUserId(userId);
        emitters.forEach((emitterId, emitter) -> {
            sendNotification(emitter, eventId, emitterId, eventName, data);
        });
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, EMessageType eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name(eventName.name())
                    .data(data));
        } catch (IOException e) {
            sseEmitterRepository.deleteById(emitterId);
            log.error("SSE 연결 오류 발생, emitterId 삭제: {}", emitterId);
        }
    }

    private String makeTimeIncludeId(Long userId) {
        return userId + "_" + System.currentTimeMillis();
    }
}
