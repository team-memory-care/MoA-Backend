package com.example.moabackend.domain.sse.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface SseEmitterRepository {
    SseEmitter save(String emitterId, SseEmitter sseEmitter);

    void saveEventCache(String eventCacheId, Object event);

    void deleteById(String emitterId);

    Map<String, SseEmitter> findAllByUserId(Long chatRoomId);

    Map<String, Object> findAllEventCacheStartWithByUserId(String chatRoomId);

    void deleteAllEventCacheStartWithId(String chatRoomId);
}
