package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QualitativePeriodSummaryQueryMapper {

    List<QualitativePeriodSummaryCandidateRow> findSummaryCandidates(
        @Param("evaluationPeriodId") Long evaluationPeriodId,
        @Param("employeeId") Long employeeId
    );
}
