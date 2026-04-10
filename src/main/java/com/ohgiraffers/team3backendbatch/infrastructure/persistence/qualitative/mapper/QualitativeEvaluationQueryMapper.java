package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationStatistics;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationTarget;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QualitativeEvaluationQueryMapper {

    List<QualitativeNormalizationTarget> findQualitativeEvaluationsForNormalization(
        @Param("evaluationPeriodId") Long evaluationPeriodId,
        @Param("force") boolean force
    );

    QualitativeNormalizationStatistics findQualitativeNormalizationStatistics(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );

    Long findLatestEvaluationPeriodIdForNormalization();
}