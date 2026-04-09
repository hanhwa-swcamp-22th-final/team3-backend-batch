package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationStatistics;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationTarget;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QualitativeEvaluationQueryMapper {

    List<QualitativeEvaluationAggregate> findQualitativeEvaluationsForAnalysis(
        @Param("evaluationPeriodId") Long evaluationPeriodId,
        @Param("employeeId") Long employeeId,
        @Param("qualitativeEvaluationId") Long qualitativeEvaluationId,
        @Param("force") boolean force,
        @Param("analysisVersion") String analysisVersion
    );

    List<QualitativeNormalizationTarget> findQualitativeEvaluationsForNormalization(
        @Param("evaluationPeriodId") Long evaluationPeriodId,
        @Param("force") boolean force
    );

    QualitativeNormalizationStatistics findQualitativeNormalizationStatistics(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );

    Long findLatestEvaluationPeriodIdForNormalization();
}