package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MonthlyQuantitativeScoreRow {

    private Long employeeId;
    private BigDecimal averageTScore;
    private BigDecimal averageProductivityScore;
    private BigDecimal averageQualityScore;
    private BigDecimal averageEquipmentResponseScore;
}
