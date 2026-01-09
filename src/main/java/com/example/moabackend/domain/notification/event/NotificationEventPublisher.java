package com.example.moabackend.domain.notification.event;

import com.example.moabackend.domain.notification.code.error.NotificationErrorCode;
import com.example.moabackend.domain.notification.dto.NotificationPayload;
import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.report.entity.type.EReportType;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
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
import java.util.Map;

import static com.example.moabackend.global.constant.Constants.*;
import static com.example.moabackend.global.constant.RedisKey.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishAfterCommit(User child, String parentName, Report report, EReportType reportType, LocalDate date) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            publishNow(child, parentName, report, reportType, date);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publishNow(child, parentName, report, reportType, date);
            }
        });
    }

    private void publishNow(User child, String parentName, Report report, EReportType reportType, LocalDate date) {
        try {

            String json;

            switch (reportType) {
                case DAILY -> {
                    json = objectMapper.writeValueAsString(
                            new NotificationPayload(
                                    child.getId(),
                                    EReportType.DAILY,
                                    String.format(DAILY_REPORT_TITLE, parentName),
                                    DAILY_REPORT_BODY,
                                    report.getId())
                    );
                }
                case WEEKLY -> {
                    json = objectMapper.writeValueAsString(
                            new NotificationPayload(
                                    child.getId(),
                                    EReportType.WEEKLY,
                                    String.format(WEEKLY_REPORT_TITLE, parentName),
                                    WEEKLY_REPORT_BODY,
                                    report.getId()
                            )
                    );
                }
                case MONTHLY -> {
                    json = objectMapper.writeValueAsString(
                            new NotificationPayload(
                                    child.getId(),
                                    EReportType.MONTHLY,
                                    String.format(MONTHLY_REPORT_TITLE, parentName),
                                    MONTHLY_REPORT_BODY,
                                    report.getId()
                            )
                    );
                }
                default -> throw new CustomException(NotificationErrorCode.NOTIFICATION_PUBLISH_ERROR);
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
}