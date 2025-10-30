package com.example.moabackend.domain.quiz.repository;

import com.example.moabackend.domain.quiz.entity.Quiz;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @Query("""
                SELECT DISTINCT q.type
                FROM Quiz q
                WHERE q.user.id = :userId
                AND DATE(q.date) = :date
            """)
    List<EQuizType> findCompletedQuizTypes(Long userId, LocalDate date);

    Optional<Quiz> findByUserIdAndDateAndType(Long userId, LocalDate date, EQuizType quizType);
}
