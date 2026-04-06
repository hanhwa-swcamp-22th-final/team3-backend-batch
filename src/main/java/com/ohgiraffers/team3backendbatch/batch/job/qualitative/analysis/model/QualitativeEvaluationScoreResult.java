package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Final qualitative evaluation raw score after evaluation-level policy is applied.
 * Normalized score and grade are finalized later in monthly normalization.
 */
@Getter
@Builder
@AllArgsConstructor
public class QualitativeEvaluationScoreResult {

    private final BigDecimal finalRawScore;
    private final BigDecimal originalSQual;
    private final BigDecimal finalSQual;
    private final BigDecimal adjustmentScore;
    private final String normalizedTier;
}