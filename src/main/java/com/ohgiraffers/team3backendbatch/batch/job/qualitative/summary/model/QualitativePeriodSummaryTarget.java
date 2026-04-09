package com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.model;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QualitativePeriodSummaryTarget {

    private final Long evaluationPeriodId;
    private final Long evaluateeId;
    private final BatchPeriodType periodType;
    private final int sourceMonthCount;
    private final BigDecimal averageRawScore;
    private final BigDecimal averageNormalizedScore;
}
