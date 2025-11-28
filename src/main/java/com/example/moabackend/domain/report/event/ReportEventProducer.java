package com.example.moabackend.domain.report.event;

import com.example.moabackend.domain.report.dto.req.ReportMessagePayload;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

import static com.example.moabackend.global.constant.RedisKey.REPORT_MESSAGE_MAP_KEY;
import static com.example.moabackend.global.constant.RedisKey.REPORT_STREAM_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportEventProducer {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishReportEvent(Long userId, String type) {
        try {
            String json = objectMapper.writeValueAsString(
                    new ReportMessagePayload(userId, LocalDate.now(), type)
            );

            redisTemplate.opsForStream().add(
                    REPORT_STREAM_KEY,
                    Map.of(REPORT_MESSAGE_MAP_KEY, json)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(GlobalErrorCode.REDIS_ERROR);
        }
    }
}
