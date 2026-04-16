package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AntiGamingSourceQueryMapper {

    List<AntiGamingProductionScoreRow> findProductionSpeedScoresByEvaluationPeriodId(
        @Param("evaluationPeriodId") Long evaluationPeriodId
    );
}
