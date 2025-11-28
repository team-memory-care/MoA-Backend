package com.example.moabackend.domain.report.service.report.monthly;

import com.example.moabackend.domain.report.dto.res.MonthlyReportResponseDto;
import com.example.moabackend.domain.user.entity.User;

public interface MonthlyReportService {
    void generateMonthlyReport(User user);

    MonthlyReportResponseDto getMonthlyReport(Long userId, int year, int month);
}
