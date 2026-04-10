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
public class PromotionHistorySnapshotEvent {

    private Long tierPromotionId;
    private Long employeeId;
    private Long reviewerId;
    private Long currentTierConfigId;
    private Long targetTierConfigId;
    private LocalDate tierConfigEffectiveDate;
    private BigDecimal tierAccumulatedPoint;
    private String promotionStatus;
    private LocalDateTime tierReviewedAt;
    private LocalDateTime occurredAt;
}
