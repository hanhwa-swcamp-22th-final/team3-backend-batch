package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MonthlyQuantitativeInspectionRow {

    private Long employeeId;
    private Long evaluationPeriodId;
    private BigDecimal tScore;
}
