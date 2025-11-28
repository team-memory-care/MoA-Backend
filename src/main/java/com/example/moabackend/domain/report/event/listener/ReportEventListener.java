package com.example.moabackend.domain.report.event.listener;

import com.example.moabackend.domain.report.event.CreateReportEvent;
import com.example.moabackend.domain.report.service.report.daily.DailyReportService;
import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportEventListener {
    private final UserRepository userRepository;
    private final DailyReportService dailyReportService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreateDailyReportEvent(CreateReportEvent event) {
        log.info("Create Daily report event received");
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        dailyReportService.generateDailyReport(user);
        log.info("Daily report generated");
    }
}
