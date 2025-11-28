package com.example.moabackend.domain.report.scheduler;

import com.example.moabackend.domain.report.entity.type.EReportType;
import com.example.moabackend.domain.report.event.ReportEventProducer;
import com.example.moabackend.domain.report.service.report.ReportFacade;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {
    private final UserRepository userRepository;
    private final ReportFacade reportFacade;
    private final ReportEventProducer reportEventProducer;

    // 매주 월요일 0시 실행
    @Scheduled(cron = "0 0 0 * * MON")
    public void scheduleWeeklyReport() {
        log.info("Start generating weekly reports");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                reportEventProducer.publishReportEvent(user.getId(), EReportType.WEEKLY.getValue());
            } catch (Exception e) {
                log.error("Failed to generate weekly report for user: {}", user.getId(), e);
            }
        }
        log.info("Finished generating weekly reports");
    }

    // 매월 1일 0시 실행
    @Scheduled(cron = "0 0 0 1 * *")
    public void scheduleMonthlyReport() {
        log.info("Start generating monthly reports");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                reportEventProducer.publishReportEvent(user.getId(), EReportType.MONTHLY.getValue());
            } catch (Exception e) {
                log.error("Failed to generate monthly report for user: {}", user.getId(), e);
            }
        }
        log.info("Finished generating monthly reports");
    }
}
