package com.example.moabackend.domain.report.converter;

import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.report.dto.res.DailyAdviceListDto;
import com.example.moabackend.domain.report.dto.res.DailyReportQuizScoreDto;
import com.example.moabackend.domain.report.dto.res.DailyReportResponseDto;
import com.example.moabackend.domain.report.entity.Advice;
import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.report.entity.ReportQuizScore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportConverter {
    // Report -> DailyReportResponseDto
    public DailyReportResponseDto toDailyAdviceListDto(Report report) {
        return new DailyReportResponseDto(
                report.getDate(),
                toReportQuizScoreDto(report.getReportQuizScores()),
                toDailyAdviceListDto(report.getAdvices())
        );
    }

    // QuizResult -> ReportQuizScore
    public List<ReportQuizScore> toReportQuizScoreList(Report report, List<QuizResult> results) {
        return results.stream().map(
                        r -> new ReportQuizScore(
                                report,
                                r.getType(),
                                r.getCorrectNumber(),
                                r.getTotalNumber()
                        ))
                .toList();
    }

    private List<DailyReportQuizScoreDto> toReportQuizScoreDto(List<ReportQuizScore> reportQuizScore) {
        return reportQuizScore.stream()
                .map(r -> new DailyReportQuizScoreDto(
                        r.getType(),
                        r.getCorrectNumber(),
                        r.getTotalNumber()))
                .toList();
    }

    private DailyAdviceListDto toDailyAdviceListDto(List<Advice> advice) {
        if (advice == null || advice.isEmpty()) {
            return new DailyAdviceListDto(List.of());
        }
        return new DailyAdviceListDto(advice.stream().map(Advice::getContent).toList());
    }


}
