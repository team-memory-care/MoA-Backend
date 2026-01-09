package com.example.moabackend.domain.report.service.report.daily;

import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.report.dto.res.DailyReportResponseDto;
import com.example.moabackend.domain.report.entity.Advice;
import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface DailyReportService {
    DailyReportResponseDto getDailyReport(Long userId, LocalDate date);

    DailyReportResponseDto getDailyReport(Report report);

    void generateDailyReport(User user, LocalDate today);

    String createDailyPrompt(List<QuizResult> results);

    List<Advice> parseAiDailyReport(String aiContent, Report report);
}
