package com.example.moabackend.domain.sse.controller;

import com.example.moabackend.domain.sse.service.SseEmitterService;
import com.example.moabackend.global.annotation.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
@Tag(name = "SSE", description = "SSE API")
public class SseController {

    private final SseEmitterService sseEmitterService;

    @Operation(summary = "SSE 구독", description = "리포트 생성 이벤트를 SSE로 구독합니다.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@UserId Long userId,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return sseEmitterService.subscribe(userId, lastEventId);
    }
}
