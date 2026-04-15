package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativePeriodSummaryProjectionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualitativePeriodSummaryProjectionRepository
    extends JpaRepository<QualitativePeriodSummaryProjectionEntity, Long> {

    Optional<QualitativePeriodSummaryProjectionEntity> findByEvaluationPeriodIdAndEvaluateeId(
        Long evaluationPeriodId,
        Long evaluateeId
    );
}
