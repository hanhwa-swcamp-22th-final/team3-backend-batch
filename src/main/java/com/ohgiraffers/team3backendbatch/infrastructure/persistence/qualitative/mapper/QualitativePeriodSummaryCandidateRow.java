package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QualitativePeriodSummaryCandidateRow {

    private Long evaluationPeriodId;
    private Long evaluateeId;
    private Long sourceMonthCount;
    private BigDecimal averageRawScore;
    private BigDecimal averageNormalizedScore;
}
