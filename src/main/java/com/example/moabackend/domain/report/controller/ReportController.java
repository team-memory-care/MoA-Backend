package com.example.moabackend.domain.report.controller;

import com.example.moabackend.domain.report.dto.res.DailyReportResponseDto;
import com.example.moabackend.domain.report.dto.res.MonthlyReportResponseDto;
import com.example.moabackend.domain.report.dto.res.WeeklyReportResponseDto;
import com.example.moabackend.domain.report.service.report.ReportFacade;
import com.example.moabackend.global.BaseResponse;
import com.example.moabackend.global.annotation.UserId;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportFacade reportFacade;

    @GetMapping("/daily")
    public BaseResponse<DailyReportResponseDto> getReport(
            @UserId Long userId,
            @RequestParam LocalDate date,
            @RequestParam(required = false) Long parentId
    ) {
        Long targetUserId = reportFacade.resolveTargetUserId(userId, parentId);
        return BaseResponse.success(reportFacade.getDailyReport(targetUserId, date));
    }

    @GetMapping("/weekly")
    public BaseResponse<WeeklyReportResponseDto> setWeeklyReport(
            @UserId Long userId,
            @RequestParam @NotNull int year,
            @RequestParam @NotNull int month,
            @RequestParam @NotNull int week,
            @RequestParam(required = false) Long parentId
    ) {
        Long targetUserId = reportFacade.resolveTargetUserId(userId, parentId);
        return BaseResponse.success(reportFacade.getWeeklyReport(targetUserId, year, month, week));
    }

    @GetMapping("/monthly")
    public BaseResponse<MonthlyReportResponseDto> getMonthlyReport(
            @UserId Long userId,
            @RequestParam @NotNull int year,
            @RequestParam @NotNull int month,
            @RequestParam(required = false) Long parentId
    ) {
        Long targetUserId = reportFacade.resolveTargetUserId(userId, parentId);
        return BaseResponse.success(reportFacade.getMonthlyReport(targetUserId, year, month));
    }
}
