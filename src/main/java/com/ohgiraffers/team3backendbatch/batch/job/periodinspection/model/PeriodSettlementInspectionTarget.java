package com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PeriodSettlementInspectionTarget {

    private final Long evaluationPeriodId;
    private final BatchPeriodType periodType;
    private final Long employeeId;
    private final int expectedMonthCount;
    private final int quantitativeMonthCount;
    private final int qualitativeMonthCount;
    private final BigDecimal quantitativeMinScore;
    private final BigDecimal quantitativeMaxScore;
    private final BigDecimal qualitativeMinScore;
    private final BigDecimal qualitativeMaxScore;
    private final BigDecimal performancePointTotal;
}
