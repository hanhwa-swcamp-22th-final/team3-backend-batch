package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QualitativeScoreQueryMapper {

    Long findLatestEvaluationPeriodIdForNormalization();

    QualitativeScoreProjectionStatisticsRow findNormalizationStatistics(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );

    List<MonthlyQualitativeScoreRow> findLatestNormalizedScoresByEvaluationPeriodId(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );

    List<MonthlyQualitativeScoreRow> findAverageMonthlyNormalizedScoresByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<MonthlyQualitativeInspectionRow> findMonthlyNormalizedInspectionRowsByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
