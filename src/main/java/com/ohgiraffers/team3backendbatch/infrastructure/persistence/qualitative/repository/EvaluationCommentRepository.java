package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.EvaluationCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluationCommentRepository extends JpaRepository<EvaluationCommentEntity, Long> {

    @Query(value = """
        SELECT
            q.evaluatee_id AS employeeId,
            ec.matched_keywords AS matchedKeywords,
            ec.matched_keyword_details AS matchedKeywordDetails
        FROM evaluation_comment ec
        JOIN qualitative_evaluation q
          ON q.qualitative_evaluation_id = ec.qualitative_evaluation_id
        WHERE q.evaluation_period_id = :evaluationPeriodId
        """, nativeQuery = true)
    List<MonthlyMatchedKeywordView> findMatchedKeywordsByEvaluationPeriodId(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );

    interface MonthlyMatchedKeywordView {
        Long getEmployeeId();
        String getMatchedKeywords();
        String getMatchedKeywordDetails();
    }
}
