package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.converter.QuizConverter;
import com.example.moabackend.domain.quiz.dto.req.QuizSaveRequestDto;
import com.example.moabackend.domain.quiz.dto.req.QuizSubmitRequestDto;
import com.example.moabackend.domain.quiz.dto.res.result.QuizRemainTypeResponseDto;
import com.example.moabackend.domain.quiz.dto.res.result.QuizSubmitResponseDto;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.QuizResult;
import com.example.moabackend.domain.quiz.entity.type.EQuizCategory;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.quiz.repository.QuizQuestionRepository;
import com.example.moabackend.domain.quiz.repository.QuizResultRepository;
import com.example.moabackend.domain.report.entity.type.EReportType;
import com.example.moabackend.domain.report.event.ReportEventProducer;
import com.example.moabackend.domain.user.code.UserErrorCode;
import com.example.moabackend.domain.user.entity.User;
import com.example.moabackend.domain.user.repository.UserRepository;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizResultServiceImpl implements QuizResultService {
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final QuizConverter quizConverter;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ReportEventProducer reportEventProducer;

    @Override
    @Transactional
    public QuizSubmitResponseDto submitAndScoreAnswer(Long userId, QuizSubmitRequestDto requestDto) {
        QuizQuestion question = quizQuestionRepository.findById(requestDto.questionId())
                .orElseThrow(() -> new CustomException(QuizErrorCode.QUIZ_NOT_FOUND));

        userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        boolean isCorrect = isAnswerCorrect(question, requestDto.userAnswer());
        int correctCount = isCorrect ? 1 : 0;

        QuizSaveRequestDto saveRequest = new QuizSaveRequestDto(1, correctCount, question.getType(), EQuizCategory.TODAY);

        saveQuizResult(userId, saveRequest);

        return new QuizSubmitResponseDto(
                question.getId(),
                question.getType(),
                isCorrect,
                question.getAnswer());
    }

    @Override
    @Transactional
    public void saveQuizResult(Long userId, QuizSaveRequestDto quizSaveRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Optional<QuizResult> quiz = quizResultRepository.findByUserIdAndDateAndTypeAndCategoryLocked(userId, LocalDate.now(), quizSaveRequestDto.type(),
                quizSaveRequestDto.category());

        if (quiz.isPresent()) {
            quiz.get().updateCorrectNumber(quizSaveRequestDto.correctNumber());
            quiz.get().updateTotalNumber(quizSaveRequestDto.totalNumber());
        } else {
            try {
                quizResultRepository.save(quizConverter.toEntity(user, quizSaveRequestDto));
            } catch (DataIntegrityViolationException e) {
                throw new CustomException(QuizErrorCode.ALREADY_SUBMITTED);
            }
        }

        updateAllTypeResultByCategory(user, LocalDate.now(), quizSaveRequestDto.category());

        if (quizSaveRequestDto.category() == EQuizCategory.TODAY && hasCompletedTodaySet(userId, LocalDate.now())) {
            reportEventProducer.publishReportEvent(userId, EReportType.DAILY);
        }
    }

    @Transactional
    public void updateAllTypeResultByCategory(User user, LocalDate date, EQuizCategory category) {
        List<QuizResult> results = quizResultRepository
                .findAllByUserIdAndDate(user.getId(), date);

        Optional<QuizResult> allOpt = results.stream()
                .filter(r -> r.getType() == EQuizType.ALL)
                .findFirst();

        int totalCorrect = results.stream()
                .filter(r -> r.getType() != EQuizType.ALL)
                .mapToInt(QuizResult::getCorrectNumber)
                .sum();

        int totalTotal = results.stream()
                .filter(r -> r.getType() != EQuizType.ALL)
                .mapToInt(QuizResult::getTotalNumber)
                .sum();

        QuizResult allResult;

        if (allOpt.isPresent()) {
            allResult = allOpt.get();
            allResult.updateCorrectNumber(totalCorrect);
            allResult.updateTotalNumber(totalTotal);
        } else {
            quizResultRepository.save(
                    QuizResult.builder()
                            .user(user)
                            .type(EQuizType.ALL)
                            .category(category)
                            .correctNumber(totalCorrect)
                            .totalNumber(totalTotal)
                            .date(date)
                            .build()
            );
        }
    }

    private boolean hasCompletedTodaySet(Long userId, LocalDate date) {
        List<EQuizType> completed = quizResultRepository.getCompletedQuizTypesByUserIdAndDateAndCategory(
                userId, date, EQuizCategory.TODAY);
        return completed.size() >= 5;
    }


    @Override
    @Transactional(readOnly = true)
    public Boolean hasCompletedAllQuiz(Long userId, LocalDate date) {
        List<EQuizType> completed = quizResultRepository.getCompletedQuizTypesByUserIdAndDateAndCategory(
                userId, date, EQuizCategory.TODAY);
        return completed.size() >= 5;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizRemainTypeResponseDto remainTypeQuiz(Long userId, LocalDate date) {
        List<EQuizType> completed = quizResultRepository.getCompletedQuizTypes(userId, date);
        List<EQuizType> remainList = Arrays.stream(EQuizType.values())
                .filter(type -> !completed.contains(type))
                .toList();
        return QuizRemainTypeResponseDto.of(remainList);
    }

    private boolean isAnswerCorrect(QuizQuestion question, String userAnswer) {
        if (userAnswer == null || userAnswer.isBlank()) {
            return false;
        }

        if (question.getType() == EQuizType.MEMORY) {
            String normalizedDb = question.getAnswer().replaceAll("[\\s,]", "").toLowerCase();
            String normalizedUser = userAnswer.replaceAll("[\\s,]", "").toLowerCase();
            return normalizedDb.equals(normalizedUser);
        }
        return question.getAnswer().trim().equalsIgnoreCase(userAnswer.trim());
    }
}