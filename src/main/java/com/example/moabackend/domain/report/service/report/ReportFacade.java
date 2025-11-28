package com.example.moabackend.domain.report.service.report;

import com.example.moabackend.domain.report.dto.res.DailyReportResponseDto;
import com.example.moabackend.domain.report.dto.res.MonthlyReportResponseDto;
import com.example.moabackend.domain.report.dto.res.WeeklyReportResponseDto;
import com.example.moabackend.domain.report.service.report.daily.DailyReportService;
import com.example.moabackend.domain.report.service.report.monthly.MonthlyReportService;
import com.example.moabackend.domain.report.service.report.weekly.WeeklyReportService;
import com.example.moabackend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReportFacade {
    private final DailyReportService dailyReportService;
    private final WeeklyReportService weeklyReportService;
    private final MonthlyReportService monthlyReportService;

    public void generateDailyReport(User user) {
        dailyReportService.generateDailyReport(user);
    }

    public DailyReportResponseDto getDailyReport(Long userId, LocalDate date) {
        return dailyReportService.getDailyReport(userId, date);
    }

    public void generateWeeklyReport(User user) {
        weeklyReportService.generateWeeklyReport(user);
    }

    public WeeklyReportResponseDto getWeeklyReport(Long userId, int year, int month, int week) {
        return weeklyReportService.getWeeklyReport(userId, year, month, week);
    }

    public void generateMonthlyReport(User user) {
        monthlyReportService.generateMonthlyReport(user);
    }

    public MonthlyReportResponseDto getMonthlyReport(Long userId, int year, int month) {
        return monthlyReportService.getMonthlyReport(userId, year, month);
    }
}
