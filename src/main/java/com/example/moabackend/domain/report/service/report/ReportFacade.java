package com.example.moabackend.domain.report.service.report;

import com.example.moabackend.domain.report.code.error.ReportErrorCode;
import com.example.moabackend.domain.report.dto.req.ReportMessagePayload;
import com.example.moabackend.domain.report.dto.res.DailyReportResponseDto;
import com.example.moabackend.domain.report.dto.res.MonthlyReportResponseDto;
import com.example.moabackend.domain.report.dto.res.WeeklyReportResponseDto;
import com.example.moabackend.domain.report.service.report.daily.DailyReportService;
import com.example.moabackend.domain.report.service.report.monthly.MonthlyReportService;
import com.example.moabackend.domain.report.service.report.weekly.WeeklyReportService;
import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReportFacade {
    private final DailyReportService dailyReportService;
    private final WeeklyReportService weeklyReportService;
    private final MonthlyReportService monthlyReportService;
    private final UserRepository userRepository;

    public void generateDailyReport(User user, LocalDate today) {
        dailyReportService.generateDailyReport(user, today);
    }

    public DailyReportResponseDto getDailyReport(Long userId, LocalDate date) {
        return dailyReportService.getDailyReport(userId, date);
    }

    public void generateWeeklyReport(User user, LocalDate today) {
        weeklyReportService.generateWeeklyReport(user, today);
    }

    public WeeklyReportResponseDto getWeeklyReport(Long userId, int year, int month, int week) {
        return weeklyReportService.getWeeklyReport(userId, year, month, week);
    }

    public void generateMonthlyReport(User user, LocalDate today) {
        monthlyReportService.generateMonthlyReport(user, today);
    }

    public MonthlyReportResponseDto getMonthlyReport(Long userId, int year, int month) {
        return monthlyReportService.getMonthlyReport(userId, year, month);
    }

    public void processReport(ReportMessagePayload msg) {
        User user = userRepository.findById(msg.userId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        switch (msg.reportType()) {
            case DAILY -> generateDailyReport(user, msg.date());
            case WEEKLY -> generateWeeklyReport(user, msg.date());
            case MONTHLY -> generateMonthlyReport(user, msg.date());
            default -> throw new CustomException(ReportErrorCode.REPORT_TYPE_NOT_FOUNT);
        }
    }
}
