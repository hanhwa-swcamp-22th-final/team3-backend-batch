package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.QuantitativeEvaluationEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuantitativeEvaluationRepository extends JpaRepository<QuantitativeEvaluationEntity, Long> {

    Optional<QuantitativeEvaluationEntity> findByEmployeeIdAndEvaluationPeriodIdAndEquipmentId(
        Long employeeId,
        Long evaluationPeriodId,
        Long equipmentId
    );

    @Query("""
        SELECT
            q.employeeId AS employeeId,
            AVG(q.tScore) AS averageTScore
        FROM QuantitativeEvaluationEntity q
        WHERE q.evaluationPeriodId = :evaluationPeriodId
          AND q.status = 'CONFIRMED'
          AND q.tScore IS NOT NULL
        GROUP BY q.employeeId
        ORDER BY q.employeeId
        """)
    List<MonthlyQuantitativeScoreView> findAverageSettledScoresByEvaluationPeriodId(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );

    interface MonthlyQuantitativeScoreView {
        Long getEmployeeId();
        BigDecimal getAverageTScore();
    }
}
