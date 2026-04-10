package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationSourceRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuantitativeEvaluationQueryMapper {

    List<QuantitativeEvaluationSourceRow> findQuantitativeSourcesForEvaluation(
        @Param("evaluationPeriodId") Long evaluationPeriodId,
        @Param("employeeId") Long employeeId,
        @Param("force") boolean force
    );
}