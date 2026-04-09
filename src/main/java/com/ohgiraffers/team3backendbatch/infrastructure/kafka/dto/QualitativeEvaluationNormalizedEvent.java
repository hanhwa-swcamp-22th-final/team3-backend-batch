package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualitativeEvaluationNormalizedEvent {

    private Long qualitativeEvaluationId;
    private BigDecimal rawScore;
    private BigDecimal sQual;
    private String grade;
    private LocalDateTime normalizedAt;
}
