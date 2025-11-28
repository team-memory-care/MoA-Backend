package com.example.moabackend.domain.report.service.report.weekly;

import com.example.moabackend.domain.report.dto.res.WeeklyReportResponseDto;
import com.example.moabackend.domain.user.entity.User;

import java.time.LocalDate;

public interface WeeklyReportService {
    void generateWeeklyReport(User user, LocalDate today);

    WeeklyReportResponseDto getWeeklyReport(Long userId, int year, int month, int week);
}
