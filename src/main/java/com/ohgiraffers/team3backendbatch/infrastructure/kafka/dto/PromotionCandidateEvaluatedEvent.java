package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCandidateEvaluatedEvent {

    private Long employeeId;
    private Long evaluationPeriodId;
    private String periodType;
    private String currentTier;
    private String targetTier;
    private Long currentTierConfigId;
    private Long targetTierConfigId;
    private BigDecimal tierAccumulatedPoint;
    private Integer promotionThreshold;
    private LocalDate tierConfigEffectiveDate;
    private String promotionStatus;
    private LocalDateTime occurredAt;
}
