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
public class SkillGrowthCalculatedEvent {

    private Long employeeId;
    private Long evaluationPeriodId;
    private String periodType;
    private String skillCategory;
    private BigDecimal skillContributionScore;
    private BigDecimal alpha;
    private LocalDate contributionDate;
    private Long sourceId;
    private String sourceType;
    private LocalDateTime occurredAt;
}
