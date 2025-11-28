package com.example.moabackend.domain.report.service.report.daily;

import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.repository.QuizResultRepository;
import com.example.moabackend.domain.report.code.error.ReportErrorCode;
import com.example.moabackend.domain.report.converter.ReportConverter;
import com.example.moabackend.domain.report.dto.res.DailyAdviceListDto;
import com.example.moabackend.domain.report.dto.res.DailyReportResponseDto;
import com.example.moabackend.domain.report.entity.Advice;
import com.example.moabackend.domain.report.entity.Report;
import com.example.moabackend.domain.report.entity.type.EReportType;
import com.example.moabackend.domain.report.repository.ReportRepository;
import com.example.moabackend.domain.report.service.ai.OpenAiService;
import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.exception.OpenAiRateLimitException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.moabackend.global.constant.Prompt.DAILY_REPORT_PROMPT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportServiceImpl implements DailyReportService {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final QuizResultRepository quizResultRepository;
    private final OpenAiService openAiService;
    private final ReportConverter reportConverter;


    @Override
    public DailyReportResponseDto getDailyReport(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Report report = reportRepository.findByUserAndTypeAndDate(user, EReportType.DAILY, date)
                .orElseThrow(() -> new CustomException(ReportErrorCode.REPORT_NOT_FOUND));
        return reportConverter.toDailyReportResponseDto(report);
    }

    @Override
    @Transactional
    public void generateDailyReport(User user, LocalDate today) {
        if (reportRepository.findByUserAndTypeAndDate(user, EReportType.DAILY, today).isPresent()) {
            return;
        }

        List<QuizResult> results = quizResultRepository.findAllByUserIdAndDate(user.getId(), today);

        String prompt = createDailyPrompt(results);
        String aiContent = openAiService.callOpenAi(prompt)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1))
                        .filter(ex -> ex instanceof OpenAiRateLimitException)
                        .maxBackoff(Duration.ofSeconds(30))
                )
                .block();

        Report report = Report.builder()
                .user(user)
                .type(EReportType.DAILY)
                .date(today)
                .reportQuizScores(new ArrayList<>())
                .advices(new ArrayList<>())
                .build();

        reportRepository.save(report);

        List<Advice> advices = parseAiDailyReport(aiContent, report);

        report.addReportQuizScore(reportConverter.toReportQuizScoreList(report, results));
        report.addAdvice(advices);
    }

    @Override
    public String createDailyPrompt(List<QuizResult> results) {
        return DAILY_REPORT_PROMPT.replace("{{quizResults}}", results.toString());
    }

    @Override
    public List<Advice> parseAiDailyReport(String aiContent, Report report) {
        try {
            objectMapper.readTree(aiContent);
            DailyAdviceListDto dailyReportResponseDto = objectMapper.readValue(aiContent, DailyAdviceListDto.class);
            return dailyReportResponseDto.advices().stream()
                    .map(a -> Advice.builder()
                            .report(report)
                            .content(a)
                            .build())
                    .toList();
        } catch (JsonProcessingException e) {
            log.warn("GPT 응답이 JSON 형식이 아닙니다. {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
