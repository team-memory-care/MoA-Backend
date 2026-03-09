package com.example.moabackend.domain.quiz.service;

import com.example.moabackend.domain.quiz.code.error.QuizErrorCode;
import com.example.moabackend.domain.quiz.converter.QuizConverter;
import com.example.moabackend.domain.quiz.dto.res.question.MemoryQuizQuestionDto;
import com.example.moabackend.domain.quiz.dto.res.question.QuizQuestionDto;
import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import com.example.moabackend.domain.quiz.repository.QuizQuestionRepository;
import com.example.moabackend.domain.quiz.dto.res.question.SpacetimeQuizQuestionDto;
import com.example.moabackend.global.exception.CustomException;
import com.example.moabackend.global.token.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class QuizQuestionServiceImpl implements QuizQuestionService {
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizConverter quizConverter;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    private static final String SPACETIME_ANSWER_KEY = "spacetime:answer:%d:%d";
    private static final long SPACETIME_ANSWER_TTL = 60 * 24; // 24시간(분)

    private static final int COUNT_PER_TYPE_TODAY = 3;
    private static final int COUNT_PER_TYPE_SET = 5;

    @Override
    public List<QuizQuestionDto> getTodayQuizSet(Long userId) {
        List<Long> ids = quizQuestionRepository.findTodayQuizIds();
        List<QuizQuestion> entities = quizQuestionRepository.findAllById(ids);

        Map<EQuizType, List<QuizQuestion>> grouped = entities.stream()
                .collect(Collectors.groupingBy(QuizQuestion::getType));

        List<QuizQuestionDto> quizSet = new ArrayList<>();

        // 모든 유형을 순회하며 데이터 수집
        for (EQuizType type : EQuizType.values()) {
            if (type == EQuizType.ALL) continue;
            List<QuizQuestion> typeEntities = grouped.getOrDefault(type, Collections.emptyList());

            if (type == EQuizType.MEMORY) {
                // 기억력 퀴즈: 3장씩 묶어 세트(3개)로 생성
                for (int i = 0, count = 0; i + 3 <= typeEntities.size() && count < COUNT_PER_TYPE_TODAY; i += 3, count++) {
                    quizSet.add(MemoryQuizQuestionDto.fromList(typeEntities.subList(i, i + 3), objectMapper));
                }
            } else {
                typeEntities.stream()
                        .limit(COUNT_PER_TYPE_TODAY)
                        .forEach(e -> {
                            QuizQuestionDto dto = quizConverter.toDto(e);
                            if (type == EQuizType.SPACETIME && dto instanceof SpacetimeQuizQuestionDto spacetimeDto) {
                                String key = String.format(SPACETIME_ANSWER_KEY, userId, spacetimeDto.questionId());
                                redisService.setData(key, spacetimeDto.answer(), SPACETIME_ANSWER_TTL);
                            }
                            quizSet.add(dto);
                        });
            }
        }

        Collections.shuffle(quizSet);
        return quizSet;
    }

    @Override
    public List<QuizQuestionDto> getQuizSetByType(Long userId, EQuizType type) {
        if (type == EQuizType.MEMORY) {
            List<QuizQuestion> allMemory = quizQuestionRepository.findAll().stream()
                    .filter(q -> q.getType() == EQuizType.MEMORY).toList();

            if (allMemory.size() < 3) throw new CustomException(QuizErrorCode.QUIZ_NOT_FOUND);

            List<QuizQuestionDto> resultSets = new ArrayList<>();
            Random random = new Random();

            for (int i = 0; i < COUNT_PER_TYPE_SET; i++) {
                List<QuizQuestion> pool = new ArrayList<>(allMemory);
                List<QuizQuestion> oneSet = new ArrayList<>();
                for (int j = 0; j < 3; j++) {
                    oneSet.add(pool.remove(random.nextInt(pool.size())));
                }
                resultSets.add(MemoryQuizQuestionDto.fromList(oneSet, objectMapper));
            }
            return resultSets;
        }

        List<Long> ids = quizQuestionRepository.findQuizIdsByType(type.name(), COUNT_PER_TYPE_SET);
        return quizQuestionRepository.findAllById(ids).stream()
                .map(e -> {
                    QuizQuestionDto dto = quizConverter.toDto(e);
                    if (type == EQuizType.SPACETIME && dto instanceof SpacetimeQuizQuestionDto spacetimeDto) {
                        String key = String.format(SPACETIME_ANSWER_KEY, userId, spacetimeDto.questionId());
                        redisService.setData(key, spacetimeDto.answer(), SPACETIME_ANSWER_TTL);
                    }
                    return dto;
                })
                .toList();
    }
}