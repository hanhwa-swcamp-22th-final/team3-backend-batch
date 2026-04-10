package com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderDifficultyResult {
    private final Long orderId;
    private final BigDecimal v1ProcessComplexity;
    private final BigDecimal v2QualityPrecision;
    private final BigDecimal v3CapacityRequirements;
    private final BigDecimal v4SpaceTimeUrgency;
    private final BigDecimal alphaNovelty;
    private final BigDecimal difficultyScore;
    private final String difficultyGrade;
    private final String analysisStatus;
    private final LocalDateTime analyzedAt;
}
