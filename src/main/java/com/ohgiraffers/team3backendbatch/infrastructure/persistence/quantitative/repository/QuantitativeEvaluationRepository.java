package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.QuantitativeEvaluationEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
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
            AVG(q.tScore) AS averageTScore,
            AVG(q.uphScore) AS averageProductivityScore,
            AVG(q.yieldScore) AS averageQualityScore,
            AVG(q.leadTimeScore) AS averageEquipmentResponseScore
        FROM QuantitativeEvaluationEntity q
        WHERE q.evaluationPeriodId = :evaluationPeriodId
          AND q.status IN :statuses
          AND q.tScore IS NOT NULL
        GROUP BY q.employeeId
        ORDER BY q.employeeId
        """)
    List<MonthlyQuantitativeScoreView> findAverageScoresByEvaluationPeriodIdAndStatusIn(
        @Param("evaluationPeriodId") Long evaluationPeriodId,
        @Param("statuses") Collection<String> statuses
    );

    @Query(
        value = """
            SELECT
                q.employee_id AS employeeId,
                AVG(q.t_score) AS averageTScore,
                AVG(q.uph_score) AS averageProductivityScore,
                AVG(q.yield_score) AS averageQualityScore,
                AVG(q.lead_time_score) AS averageEquipmentResponseScore
            FROM quantitative_evaluation q
            JOIN batch_projection.evaluation_period_projection ep
              ON ep.evaluation_period_id = q.evaluation_period_id
            WHERE ep.status = 'CONFIRMED'
              AND DATEDIFF(ep.end_date, ep.start_date) + 1 BETWEEN 28 AND 31
              AND ep.start_date >= :startDate
              AND ep.end_date <= :endDate
              AND q.status = 'CONFIRMED'
              AND q.t_score IS NOT NULL
            GROUP BY q.employee_id
            ORDER BY q.employee_id
            """,
        nativeQuery = true
    )
    List<MonthlyQuantitativeScoreView> findAverageMonthlySettledScoresByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query(
        value = """
            SELECT
                q.employee_id AS employeeId,
                q.evaluation_period_id AS evaluationPeriodId,
                q.t_score AS tScore
            FROM quantitative_evaluation q
            JOIN batch_projection.evaluation_period_projection ep
              ON ep.evaluation_period_id = q.evaluation_period_id
            WHERE ep.status = 'CONFIRMED'
              AND DATEDIFF(ep.end_date, ep.start_date) + 1 BETWEEN 28 AND 31
              AND ep.start_date >= :startDate
              AND ep.end_date <= :endDate
              AND q.status = 'CONFIRMED'
              AND q.t_score IS NOT NULL
            ORDER BY q.employee_id, q.evaluation_period_id
            """,
        nativeQuery = true
    )
    List<MonthlyQuantitativeInspectionRow> findMonthlySettledInspectionRowsByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    default List<MonthlyQuantitativeScoreView> findAverageSettledScoresByEvaluationPeriodId(Long evaluationPeriodId) {
        return findAverageScoresByEvaluationPeriodIdAndStatusIn(evaluationPeriodId, List.of("CONFIRMED"));
    }

    interface MonthlyQuantitativeScoreView {
        Long getEmployeeId();
        BigDecimal getAverageTScore();
        BigDecimal getAverageProductivityScore();
        BigDecimal getAverageQualityScore();
        BigDecimal getAverageEquipmentResponseScore();
    }

    interface MonthlyQuantitativeInspectionRow {
        Long getEmployeeId();
        Long getEvaluationPeriodId();
        BigDecimal getTScore();
    }
}
