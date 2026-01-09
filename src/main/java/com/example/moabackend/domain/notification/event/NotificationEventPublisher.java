package com.example.moabackend.domain.notification.event;

import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.report.entity.type.EReportType;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Map;

import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishAfterCommit(User child, String parentName, Long reportId, EReportType reportType, LocalDate date) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            publishNow(child, parentName, reportId, reportType, date);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publishNow(child, parentName, reportId, reportType, date);
            }
        });
    }

    private void publishNow(User child, String parentName, Long reportId, EReportType reportType, LocalDate date) {
        try {

            String json;

            switch (reportType) {
                case DAILY -> {
                    json = objectMapper.writeValueAsString(
                            new NotificationPayload(
                                    child.getId(),
                                    EReportType.DAILY,
                                    setDailyTitle(date),
                                    setDailyBody(parentName),
                                    reportId)
                    );
                }
                case WEEKLY -> {
                    json = objectMapper.writeValueAsString(
                            new NotificationPayload(
                                    child.getId(),
                                    EReportType.WEEKLY,
                                    setWeeklyTitle(date),
                                    setWeeklyBody(parentName, date),
                                    reportId
                            )
                    );
                }
                case MONTHLY -> {
                    json = objectMapper.writeValueAsString(
                            new NotificationPayload(
                                    child.getId(),
                                    EReportType.MONTHLY,
                                    setMonthlyTitle(date),
                                    setMonthlyBody(parentName, date),
                                    reportId
                            )
                    );
                }
                default -> json = null;
            }

            redisTemplate.opsForStream().add(
                    StreamRecords.newRecord()
                            .ofMap(Map.of(
                                    FIELD_PAYLOAD, json,
                                    FIELD_RETRY, "0"
                            ))
                            .withStreamKey(NOTI_STREAM_KEY),
                    RedisStreamCommands.XAddOptions.maxlen(MAX_LEN).approximateTrimming(true)
            );

            log.info("Published notification event. userId={}, type={}, date={}", child.getId(), reportType, date);

        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
        }
    }

    private String setDailyTitle(LocalDate date) {
        return String.format("%d월 %d일 일간 리포트", date.getMonthValue(), date.getDayOfMonth());
    }

    private String setDailyBody(String name) {
        return String.format("%s님의 오늘의 리포트를 확인해보세요", name);
    }

    private String setWeeklyTitle(LocalDate date) {
        return String.format("%d월 %d주차 주간 리포트", date.getMonthValue(), date.get(WeekFields.ISO.weekOfMonth()));
    }

    private String setWeeklyBody(String parentName, LocalDate date) {
        return String.format("%s님의 %d월 %d주차 리포트를 확인해보세요", parentName, date.getMonthValue(), date.get(WeekFields.ISO.weekOfMonth()));
    }

    private String setMonthlyTitle(LocalDate date) {
        return String.format("%d년 %d월 월간 리포트", date.getYear(), date.getMonthValue());
    }

    private String setMonthlyBody(String parentName, LocalDate date) {
        return String.format("%s님의 %d년 %d월 리포트를 확인해보세요", parentName, date.getYear(), date.getMonthValue());
    }
}