package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluationPeriodProjectionRepository extends JpaRepository<EvaluationPeriodProjectionEntity, Long> {

    @Query(
        value = """
            SELECT *
            FROM batch_projection.evaluation_period_projection ep
            WHERE ep.status = 'CONFIRMED'
              AND DATEDIFF(ep.end_date, ep.start_date) + 1 BETWEEN :minDays AND :maxDays
            ORDER BY ep.end_date DESC, ep.evaluation_period_id DESC
            LIMIT 1
            """,
        nativeQuery = true
    )
    Optional<EvaluationPeriodProjectionEntity> findLatestConfirmedPeriodByInclusiveDaysBetween(
        @Param("minDays") int minDays,
        @Param("maxDays") int maxDays
    );

    default Optional<EvaluationPeriodProjectionEntity> findLatestConfirmedMonthlyPeriod() {
        return findLatestConfirmedPeriodByInclusiveDaysBetween(28, 31);
    }

    default Optional<EvaluationPeriodProjectionEntity> findLatestConfirmedPeriod(BatchPeriodType periodType) {
        return switch (periodType) {
            case WEEK -> findLatestConfirmedPeriodByInclusiveDaysBetween(5, 8);
            case MONTH -> findLatestConfirmedPeriodByInclusiveDaysBetween(28, 31);
            case QUARTER -> findLatestConfirmedPeriodByInclusiveDaysBetween(89, 93);
            case HALF_YEAR -> findLatestConfirmedPeriodByInclusiveDaysBetween(180, 184);
            case YEAR -> findLatestConfirmedPeriodByInclusiveDaysBetween(364, 366);
        };
    }

    @Query(
        value = """
            SELECT *
            FROM batch_projection.evaluation_period_projection ep
            WHERE ep.status = 'CONFIRMED'
              AND ep.start_date >= :startDate
              AND ep.end_date <= :endDate
              AND DATEDIFF(ep.end_date, ep.start_date) + 1 BETWEEN 28 AND 31
            ORDER BY ep.start_date ASC, ep.evaluation_period_id ASC
            """,
        nativeQuery = true
    )
    List<EvaluationPeriodProjectionEntity> findConfirmedMonthlyPeriodsWithin(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
