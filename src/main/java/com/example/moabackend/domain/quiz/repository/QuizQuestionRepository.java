package com.example.moabackend.domain.quiz.repository;

import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import com.example.moabackend.domain.quiz.entity.type.EQuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    @Query(value = """
                SELECT * FROM quiz_question q
                WHERE q.type = :type
                ORDER BY RAND()
                LIMIT :count
            """, nativeQuery = true)
    List<QuizQuestion> findRandomNByType(@Param("type") EQuizType type, @Param("count") int count);
}