package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MonthlyQualitativeInspectionRow {

    private Long employeeId;
    private Long evaluationPeriodId;
    private BigDecimal normalizedScore;
}
