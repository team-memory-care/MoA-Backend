package com.example.moabackend.domain.sse.service;

import com.example.moabackend.domain.sse.dto.EMessageType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterService {
    SseEmitter subscribe(Long userId, String lastEventId);

    void sendToClient(Long userId, EMessageType eventName, Object data);
}
