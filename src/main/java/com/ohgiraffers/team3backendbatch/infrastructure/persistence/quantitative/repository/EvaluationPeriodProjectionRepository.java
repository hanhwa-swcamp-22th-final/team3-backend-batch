package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EvaluationPeriodProjectionRepository extends JpaRepository<EvaluationPeriodProjectionEntity, Long> {

    @Query(
        value = """
            SELECT *
            FROM batch_projection.evaluation_period_projection ep
            WHERE ep.status = 'CONFIRMED'
              AND TIMESTAMPDIFF(MONTH, ep.start_date, ep.end_date) = 0
            ORDER BY ep.end_date DESC, ep.evaluation_period_id DESC
            LIMIT 1
            """,
        nativeQuery = true
    )
    Optional<EvaluationPeriodProjectionEntity> findLatestConfirmedMonthlyPeriod();
}
