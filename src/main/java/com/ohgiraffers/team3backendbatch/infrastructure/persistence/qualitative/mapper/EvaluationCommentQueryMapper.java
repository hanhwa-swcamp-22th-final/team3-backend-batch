package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EvaluationCommentQueryMapper {

    List<MonthlyMatchedKeywordRow> findMatchedKeywordsByEvaluationPeriodId(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );

    List<MonthlyMatchedKeywordRow> findMatchedKeywordsByPeriodRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
