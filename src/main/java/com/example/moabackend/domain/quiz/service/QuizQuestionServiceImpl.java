package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.converter.QuizConverter;
import com.example.moabackend.domain.quiz.dto.res.QuizQuestionDto;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.quiz.repository.QuizQuestionRepository;
import com.example.moabackend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizQuestionServiceImpl implements QuizQuestionService {
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizConverter quizConverter;

    private static final int COUNT_PER_TYPE_TODAY = 3;
    private static final int COUNT_PER_TYPE_SET = 5;

    @Override
    public List<QuizQuestionDto> getTodayQuizSet(Long userId) {
        List<QuizQuestionDto> quizSet = new ArrayList<>();
        for (EQuizType type : EQuizType.values()) {
            List<QuizQuestion> entities = quizQuestionRepository.findRandomNByType(type.name(), COUNT_PER_TYPE_TODAY);

            entities.stream()
                    .map(quizConverter::toDto)
                    .forEach(quizSet::add);
        }
        if (quizSet.size() < EQuizType.values().length * COUNT_PER_TYPE_TODAY) {
            throw new CustomException(QuizErrorCode.QUIZ_NOT_FOUND);
        }
        Collections.shuffle(quizSet);
        return quizSet;
    }

    @Override
    public List<QuizQuestionDto> getQuizSetByType(Long userId, EQuizType type) {
        List<QuizQuestion> entities = quizQuestionRepository.findRandomNByType(type.name(), COUNT_PER_TYPE_SET);

        if (entities.isEmpty()) {
            throw new CustomException(QuizErrorCode.QUIZ_NOT_FOUND);
        }
        return entities.stream().map(quizConverter::toDto).collect(Collectors.toList());
    }
}
