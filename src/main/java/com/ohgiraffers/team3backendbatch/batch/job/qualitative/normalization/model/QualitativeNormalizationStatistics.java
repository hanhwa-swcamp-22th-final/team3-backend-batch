package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QualitativeNormalizationStatistics {

    private final Long sampleCount;
    private final BigDecimal meanScore;
    private final BigDecimal stddevScore;
}