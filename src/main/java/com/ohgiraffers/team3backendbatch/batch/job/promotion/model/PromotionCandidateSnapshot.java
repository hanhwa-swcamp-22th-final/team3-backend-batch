package com.ohgiraffers.team3backendbatch.batch.job.promotion.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PromotionCandidateSnapshot {

    private final Long employeeId;
    private final Long evaluationPeriodId;
    private final String periodType;
    private final String currentTier;
    private final String targetTier;
    private final Long currentTierConfigId;
    private final Long targetTierConfigId;
    private final BigDecimal tierAccumulatedPoint;
    private final Integer promotionThreshold;
    private final LocalDate tierConfigEffectiveDate;
    private final LocalDateTime occurredAt;
}
