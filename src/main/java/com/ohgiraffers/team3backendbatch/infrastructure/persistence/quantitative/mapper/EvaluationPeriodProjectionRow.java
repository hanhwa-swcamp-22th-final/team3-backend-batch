package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationPeriodProjectionRow {

    private Long evaluationPeriodId;
    private Long algorithmVersionId;
    private Integer evaluationYear;
    private Integer evaluationSequence;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String policyConfig;
    private String parameters;
    private String referenceValues;
}
