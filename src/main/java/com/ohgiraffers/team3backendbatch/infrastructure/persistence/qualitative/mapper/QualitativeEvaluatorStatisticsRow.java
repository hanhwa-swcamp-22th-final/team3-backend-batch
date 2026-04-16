package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QualitativeEvaluatorStatisticsRow {

    private Long evaluatorId;
    private Long sampleCount;
    private BigDecimal meanScore;
}
