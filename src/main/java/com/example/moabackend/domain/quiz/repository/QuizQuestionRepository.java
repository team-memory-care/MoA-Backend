package com.example.moabackend.domain.quiz.repository;

import com.example.moabackend.domain.quiz.entity.QuizQuestion;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    @Query(value = "SELECT qq.* FROM quiz_question qq WHERE qq.type = :type ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<QuizQuestion> findQuizSetByType(@Param("type") String type, @Param("count") int count);

    @Query(value = """
            
            (SELECT qq.* FROM quiz_question qq WHERE qq.type = 'PERSISTENCE' ORDER BY RANDOM() LIMIT 3)
               UNION ALL (SELECT qq.* FROM quiz_question qq WHERE qq.type = 'MEMORY' ORDER BY RANDOM() LIMIT 3)
                    UNION ALL (SELECT qq.* FROM quiz_question qq WHERE qq.type = 'LINGUISTIC' ORDER BY RANDOM() LIMIT 3)
                    UNION ALL (SELECT qq.* FROM quiz_question qq WHERE qq.type = 'ATTENTION' ORDER BY RANDOM() LIMIT 3)
                    UNION ALL (SELECT qq.* FROM quiz_question qq WHERE qq.type = 'SPACETIME' ORDER BY RANDOM() LIMIT 3)
            """, nativeQuery = true)
    List<QuizQuestion> findTodayQuizSet();
}