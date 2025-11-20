package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.converter.QuizConverter;
import com.example.moabackend.domain.quiz.dto.res.question.QuizQuestionDto;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.quiz.repository.QuizQuestionRepository;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class QuizQuestionServiceImpl implements QuizQuestionService {
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizConverter quizConverter;

    private static final int COUNT_PER_TYPE_TODAY = 3;
    private static final int COUNT_PER_TYPE_SET = 5;

    @Override
    public List<QuizQuestionDto> getTodayQuizSet(Long userId) {

        List<Long> ids = quizQuestionRepository.findTodayQuizIds();
        List<QuizQuestion> entities = quizQuestionRepository.findAllById(ids);

        if (entities.size() < EQuizType.values().length * COUNT_PER_TYPE_TODAY) {
            throw new CustomException(QuizErrorCode.QUIZ_NOT_FOUND);
        }

        // [테스트용 코드 추가] 정답 로그 출력
        entities.forEach(q -> log.info(">>> [TEST_LOG] ID: {}, Type: {}, Answer: {}", q.getId(), q.getType(), q.getAnswer()));

        List<QuizQuestionDto> quizSet = entities.stream().map(quizConverter::toDto).collect(Collectors.toList());
        Collections.shuffle(quizSet);
        return quizSet;
    }

    @Override
    public List<QuizQuestionDto> getQuizSetByType(Long userId, EQuizType type) {
        List<Long> ids = quizQuestionRepository.findQuizIdsByType(type.name(), COUNT_PER_TYPE_SET);
        List<QuizQuestion> entities = quizQuestionRepository.findAllById(ids);
        if (entities.isEmpty()) {
            throw new CustomException(QuizErrorCode.QUIZ_NOT_FOUND);
        }
        // [테스트용 코드 추가] 정답 로그 출력
        entities.forEach(q -> log.info(">>> [TEST_LOG] ID: {}, Type: {}, Answer: {}", q.getId(), q.getType(), q.getAnswer()));
        return entities.stream().map(quizConverter::toDto).collect(Collectors.toList());
    }
}
