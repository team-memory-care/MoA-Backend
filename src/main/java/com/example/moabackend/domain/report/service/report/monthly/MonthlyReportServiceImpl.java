package com.example.moabackend.domain.report.service.report.monthly;

import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.quiz.repository.QuizResultRepository;
import com.example.moabackend.domain.report.dto.res.MonthlyReportResponseDto;
import com.example.moabackend.domain.report.dto.res.MonthlyScoreDto;
import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.report.entity.type.EReportType;
import com.example.moabackend.domain.report.repository.ReportRepository;
import com.example.moabackend.domain.report.service.ai.OpenAiService;
import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.code.GlobalErrorCode;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.exception.OpenAiRateLimitException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.example.moabackend.global.constant.Prompt.MONTHLY_REPORT_PROMPT;

@Service
@RequiredArgsConstructor
public class MonthlyReportServiceImpl implements MonthlyReportService {
    private final ObjectMapper objectMapper;
    private final ReportRepository reportRepository;
    private final QuizResultRepository quizResultRepository;
    private final OpenAiService openAiService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void generateMonthlyReport(User user, LocalDate today) {
        LocalDate startOfMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfMonth = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        if (reportRepository.findByUserAndTypeAndDate(user, EReportType.MONTHLY, startOfMonth).isPresent()) {
            return;
        }

        List<QuizResult> thisMonth =
                quizResultRepository.findAllByUserIdAndDateBetween(
                        user.getId(), startOfMonth, endOfMonth);

        LocalDate lastMonthStart = startOfMonth.minusMonths(1);
        LocalDate lastMonthEnd = lastMonthStart.with(TemporalAdjusters.lastDayOfMonth());

        List<QuizResult> lastMonth =
                quizResultRepository.findAllByUserIdAndDateBetween(
                        user.getId(), lastMonthStart, lastMonthEnd);

        // scoreResult 생성
        Map<EQuizType, List<MonthlyScoreDto>> scoreResult =
                buildMonthlyScoreResult(thisMonth, lastMonth, startOfMonth);

        int completeRate = calcCompleteRate(thisMonth, startOfMonth, endOfMonth);
        int correctRate = calcCorrectRate(thisMonth);

        String prompt = createMonthlyPrompt(completeRate, correctRate, scoreResult);
        String aiContent = openAiService.callOpenAi(prompt)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1))
                        .filter(ex -> ex instanceof OpenAiRateLimitException)
                        .maxBackoff(Duration.ofSeconds(30))
                )
                .block();

        MonthlyReportResponseDto aiResponse = parseAiResponse(aiContent);

        Report report = Report.builder()
                .user(user)
                .type(EReportType.MONTHLY)
                .date(startOfMonth)
                .oneLineReview(aiResponse.oneLineReview())
                .diagnosis(aiResponse.diagnosis())
                .strategy(aiResponse.longTermStrategy())
                .completeRate(completeRate)
                .correctRate(correctRate)
                .build();

        reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyReportResponseDto getMonthlyReport(Long userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        LocalDate reportDate = LocalDate.of(year, month, 1);
//        Report report = reportRepository.findByUserAndTypeAndDate(
//                user, EReportType.MONTHLY, reportDate
//        ).orElseThrow(() -> new CustomException(ReportErrorCode.REPORT_NOT_FOUND));

        Report report = reportRepository.findByUserAndTypeAndDate(user, EReportType.MONTHLY, reportDate)
                .orElse(null);

        if (report == null) {
            return null;
        }

        LocalDate startOfMonth = reportDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = reportDate.with(TemporalAdjusters.lastDayOfMonth());

        List<QuizResult> thisMonth = quizResultRepository.findAllByUserIdAndDateBetween(
                user.getId(), startOfMonth, endOfMonth
        );

        LocalDate lastMonthStart = startOfMonth.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastMonthEnd = lastMonthStart.with(TemporalAdjusters.lastDayOfMonth());

        List<QuizResult> lastMonth = quizResultRepository.findAllByUserIdAndDateBetween(
                user.getId(), lastMonthStart, lastMonthEnd
        );

        Map<EQuizType, List<MonthlyScoreDto>> scoreResult =
                buildMonthlyScoreResult(thisMonth, lastMonth, startOfMonth);

        // --- 6. 최종 DTO 생성 ---
        return new MonthlyReportResponseDto(
                report.getOneLineReview(),
                report.getCompleteRate(),
                report.getCorrectRate(),
                report.getDiagnosis(),
                scoreResult,
                report.getStrategy()
        );
    }

    private Map<EQuizType, List<MonthlyScoreDto>> buildMonthlyScoreResult(
            List<QuizResult> thisMonth,
            List<QuizResult> lastMonth,
            LocalDate monthStart
    ) {
        Map<EQuizType, List<MonthlyScoreDto>> result = new EnumMap<>(EQuizType.class);

        int totalWeeks = monthStart.lengthOfMonth() / 7 + 1;

        for (EQuizType type : EQuizType.values()) {
            List<MonthlyScoreDto> weekly = new ArrayList<>();

            for (int weekIdx = 1; weekIdx <= totalWeeks; weekIdx++) {
                long score = findAverageScoreForWeek(thisMonth, type, weekIdx);
                long lastScore = findAverageScoreForWeek(lastMonth, type, weekIdx);

                weekly.add(new MonthlyScoreDto(weekIdx, score, lastScore));
            }

            result.put(type, weekly);
        }

        return result;
    }

    private long findAverageScoreForWeek(
            List<QuizResult> results,
            EQuizType type,
            int weekIndex
    ) {
        List<QuizResult> filtered = results.stream()
                .filter(r -> r.getType() == type)
                .filter(r -> getWeekIndex(r.getDate()) == weekIndex)
                .toList();

        if (filtered.isEmpty()) return 0;

        double avg = filtered.stream()
                .mapToDouble(r -> (r.getCorrectNumber() * 100.0) / r.getTotalNumber())
                .average()
                .orElse(0);

        return Math.round(avg);
    }

    private int getWeekIndex(LocalDate date) {
        return date.get(ChronoField.ALIGNED_WEEK_OF_MONTH);
    }

    private int calcCompleteRate(List<QuizResult> results, LocalDate startOfMonth, LocalDate endOfMonth) {
        long totalDays = ChronoUnit.DAYS.between(startOfMonth, endOfMonth) + 1;

        long participatedDays = results.stream()
                .map(QuizResult::getDate)
                .distinct()
                .count();

        return (int) Math.round((participatedDays * 100.0) / totalDays);
    }

    private int calcCorrectRate(List<QuizResult> results) {
        int totalCorrect = results.stream()
                .mapToInt(QuizResult::getCorrectNumber)
                .sum();

        int totalQuestions = results.stream()
                .mapToInt(QuizResult::getTotalNumber)
                .sum();

        if (totalQuestions == 0) return 0;

        return (int) Math.round((totalCorrect * 100.0) / totalQuestions);
    }

    private String createMonthlyPrompt(int completeRate, int correctRate, Map<EQuizType, List<MonthlyScoreDto>> scoreResult) {
        String scoreResultJson;
        try {
            scoreResultJson = objectMapper.writeValueAsString(scoreResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Monthly scoreResult JSON serialization failed", e);
        }

        return MONTHLY_REPORT_PROMPT
                .replace("{{completeRate}}", String.valueOf(completeRate))
                .replace("{{correctRate}}", String.valueOf(correctRate))
                .replace("{{weeklyAveragesJson}}", scoreResultJson);
    }

    private MonthlyReportResponseDto parseAiResponse(String aiContent) {
        try {
            return objectMapper.readValue(aiContent, MonthlyReportResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(GlobalErrorCode.BAD_JSON);
        }
    }
}
