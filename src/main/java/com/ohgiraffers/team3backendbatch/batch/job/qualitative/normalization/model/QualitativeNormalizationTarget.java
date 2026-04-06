package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QualitativeNormalizationTarget {

    private final Long evaluationId;
    private final BigDecimal rawScore;
}