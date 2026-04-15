package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualitativeScoreProjectionRepository extends JpaRepository<QualitativeScoreProjectionEntity, Long> {

    List<QualitativeScoreProjectionEntity> findAllByQualitativeEvaluationIdIn(Collection<Long> qualitativeEvaluationIds);

    List<QualitativeScoreProjectionEntity> findByEvaluationPeriodIdAndRawScoreIsNotNullAndNormalizedScoreIsNullOrderByEvaluationLevelAscQualitativeEvaluationIdAsc(
        Long evaluationPeriodId
    );

    List<QualitativeScoreProjectionEntity> findByEvaluationPeriodIdAndRawScoreIsNotNullOrderByEvaluationLevelAscQualitativeEvaluationIdAsc(
        Long evaluationPeriodId
    );
}
