package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QualitativeNormalizationResult {

    private final Long evaluationId;
    private final BigDecimal originalRawScore;
    private final BigDecimal rawScore;
    private final BigDecimal sQual;
    private final String grade;
    private final boolean biasCorrected;
    private final String biasType;
    private final BigDecimal evaluatorAverage;
    private final BigDecimal companyAverage;
    private final BigDecimal alphaBias;
}
