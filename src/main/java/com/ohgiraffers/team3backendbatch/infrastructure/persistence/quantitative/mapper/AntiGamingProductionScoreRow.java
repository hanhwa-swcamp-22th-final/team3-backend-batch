package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AntiGamingProductionScoreRow {

    private Long employeeId;
    private BigDecimal productionSpeedScore;
}
