package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QualitativeScoreProjectionStatisticsRow {

    private Long sampleCount;
    private BigDecimal meanScore;
    private BigDecimal stddevScore;
}
