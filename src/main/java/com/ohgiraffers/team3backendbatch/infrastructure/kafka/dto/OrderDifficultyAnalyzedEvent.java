package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDifficultyAnalyzedEvent {
    private Long orderId;
    private BigDecimal v1ProcessComplexity;
    private BigDecimal v2QualityPrecision;
    private BigDecimal v3CapacityRequirements;
    private BigDecimal v4SpaceTimeUrgency;
    private BigDecimal alphaNovelty;
    private BigDecimal difficultyScore;
    private String difficultyGrade;
    private String analysisStatus;
    private LocalDateTime analyzedAt;
}
