package com.example.moabackend.domain.report.service.report.monthly;

import com.example.moabackend.domain.report.dto.res.MonthlyReportResponseDto;
import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.user.entity.User;

import java.time.LocalDate;

public interface MonthlyReportService {
    void generateMonthlyReport(User user, LocalDate today);

    MonthlyReportResponseDto getMonthlyReport(Long userId, int year, int month);

    MonthlyReportResponseDto getMonthlyReport(Report report);
}
