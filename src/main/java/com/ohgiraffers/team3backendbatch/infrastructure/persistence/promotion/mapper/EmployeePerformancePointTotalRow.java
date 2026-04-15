package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.mapper;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeePerformancePointTotalRow {

    private Long employeeId;
    private BigDecimal totalPoint;
}
