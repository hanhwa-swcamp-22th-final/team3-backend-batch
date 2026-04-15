package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuantitativeEvaluationAggregateQueryMapper {

    List<MonthlyQuantitativeScoreRow> findAverageScoresByEvaluationPeriodIdAndStatusIn(
        @Param("evaluationPeriodId") Long evaluationPeriodId,
        @Param("statuses") Collection<String> statuses
    );

    List<MonthlyQuantitativeScoreRow> findAverageMonthlySettledScoresByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<MonthlyQuantitativeInspectionRow> findMonthlySettledInspectionRowsByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
