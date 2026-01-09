package com.example.moabackend.domain.report.service.report.weekly;

import com.example.moabackend.domain.notification.event.NotificationEventPublisher;
import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.quiz.repository.QuizResultRepository;
import com.example.moabackend.domain.report.dto.res.WeeklyReportResponseDto;
import com.example.moabackend.domain.report.dto.res.WeeklyScoreDto;
import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.report.entity.type.EDayOfWeekType;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.retry.Retry;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.example.moabackend.global.constant.Prompt.WEEKLY_REPORT_PROMPT;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportServiceImpl implements WeeklyReportService {
    private final ObjectMapper objectMapper;
    private final ReportRepository reportRepository;
    private final QuizResultRepository quizResultRepository;
    private final OpenAiService openAiService;
    private final UserRepository userRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    @Transactional
    public void generateWeeklyReport(User user, LocalDate today) {
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        if (reportRepository.findByUserAndTypeAndDate(user, EReportType.WEEKLY, startOfWeek).isPresent()) {
            return;
        }

        // 이번 주 결과
        List<QuizResult> results = quizResultRepository.findAllByUserIdAndDateBetween(
                user.getId(), startOfWeek, endOfWeek
        );

        // 지난 주 결과
        LocalDate lastWeekStart = startOfWeek.minusWeeks(1);
        LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
        List<QuizResult> lastWeekResults = quizResultRepository.findAllByUserIdAndDateBetween(
                user.getId(), lastWeekStart, lastWeekEnd
        );

        // Weekly ScoreResult 생성
        Map<EQuizType, List<WeeklyScoreDto>> scoreResult = buildWeeklyScoreResult(results, lastWeekResults, startOfWeek);

        // completeRate, correctRate 계산
        int completeRate = calcCompleteRate(results);
        int correctRate = calcCorrectRate(results);

        // 프롬프트 생성
        String prompt = weeklyPrompt(completeRate, correctRate, scoreResult);

        String aiContent = openAiService.callOpenAi(prompt)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1))
                        .filter(ex -> ex instanceof OpenAiRateLimitException)
                        .maxBackoff(Duration.ofSeconds(30))
                )
                .block();
        WeeklyReportResponseDto aiDto = parseAiWeeklyReport(aiContent);

        // Report 엔티티 저장
        Report report = Report.builder()
                .user(user)
                .type(EReportType.WEEKLY)
                .date(startOfWeek)
                .oneLineReview(aiDto.oneLineReview())
                .completeRate(completeRate)
                .correctRate(correctRate)
                .diagnosis(aiDto.diagnosis())
                .strategy(aiDto.nextWeekStrategy())
                .reportQuizScores(new ArrayList<>())
                .advices(new ArrayList<>())
                .build();

        reportRepository.save(report);

        List<User> childs = userRepository.findAllByParents_Id(user.getId());

        for (User child : childs) {
            notificationEventPublisher.publishAfterCommit(child, user.getName(), report, EReportType.WEEKLY, today);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyReportResponseDto getWeeklyReport(Long userId, int year, int month, int week) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 이번주 기간 계산
        LocalDate startOfWeek = LocalDate.of(year, month, 1)
                .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))
                .plusWeeks(week - 1);

        Report report = reportRepository.findByUserAndTypeAndDate(user, EReportType.WEEKLY, startOfWeek)
                .orElse(null);

        if (report == null) {
            return null;
        }

        return getWeeklyReport(report);
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyReportResponseDto getWeeklyReport(Report report) {
        LocalDate startOfWeek = report.getDate();
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<QuizResult> thisWeek = quizResultRepository.findAllByUserIdAndDateBetween(
                report.getUser().getId(), startOfWeek, endOfWeek);

        LocalDate lastWeekStart = startOfWeek.minusWeeks(1);
        LocalDate lastWeekEnd = endOfWeek.minusWeeks(1);

        List<QuizResult> lastWeek = quizResultRepository.findAllByUserIdAndDateBetween(
                report.getUser().getId(), lastWeekStart, lastWeekEnd);

        Map<EQuizType, List<WeeklyScoreDto>> scoreResult = buildWeeklyScoreResult(thisWeek, lastWeek, startOfWeek);

        return new WeeklyReportResponseDto(
                report.getOneLineReview(),
                report.getCompleteRate(),
                report.getCorrectRate(),
                scoreResult,
                report.getDiagnosis(),
                report.getStrategy()
        );
    }

    private Map<EQuizType, List<WeeklyScoreDto>> buildWeeklyScoreResult(
            List<QuizResult> thisWeek,
            List<QuizResult> lastWeek,
            LocalDate thisWeekStart
    ) {
        Map<EQuizType, List<WeeklyScoreDto>> result = new EnumMap<>(EQuizType.class);

        for (EQuizType type : EQuizType.values()) {
            List<WeeklyScoreDto> dailyScores = new ArrayList<>();

            for (EDayOfWeekType day : EDayOfWeekType.values()) {

                int dayIndex = day.ordinal();
                LocalDate thisDate = thisWeekStart.plusDays(dayIndex);
                LocalDate lastDate = thisDate.minusWeeks(1);

                long score = findScore(thisWeek, type, thisDate);
                long lastScore = findScore(lastWeek, type, lastDate);

                dailyScores.add(new WeeklyScoreDto(day.getDay(), score, lastScore));
            }

            result.put(type, dailyScores);
        }

        return result;
    }

    private int calcCompleteRate(List<QuizResult> results) {
        int expected = 7 * EQuizType.values().length;
        return (int) (((double) results.size() / expected) * 100);
    }

    private int calcCorrectRate(List<QuizResult> results) {
        double avg = results.stream()
                .mapToDouble(r -> (double) r.getCorrectNumber() / r.getTotalNumber())
                .average().orElse(0);

        return (int) (avg * 100);
    }

    private String weeklyPrompt(int completeRate, int correctRate, Map<EQuizType, List<WeeklyScoreDto>> scoreResult) {
        String scoreResultJson;
        try {
            scoreResultJson = objectMapper.writeValueAsString(scoreResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Weekly scoreResult JSON serialization failed", e);
        }

        return WEEKLY_REPORT_PROMPT
                .replace("{{correctRate}}", String.valueOf(correctRate))
                .replace("{{completeRate}}", String.valueOf(completeRate))
                .replace("{{scoreResultJson}}", scoreResultJson);
    }

    private WeeklyReportResponseDto parseAiWeeklyReport(String aiContent) {
        try {
            return objectMapper.readValue(aiContent, WeeklyReportResponseDto.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException(GlobalErrorCode.BAD_JSON);
        }
    }

    private long findScore(List<QuizResult> list, EQuizType type, LocalDate date) {
        return list.stream()
                .filter(r -> r.getType().equals(type) && r.getDate().equals(date))
                .findFirst()
                .map(r -> (long) r.getCorrectNumber())
                .orElse(0L);
    }
}
