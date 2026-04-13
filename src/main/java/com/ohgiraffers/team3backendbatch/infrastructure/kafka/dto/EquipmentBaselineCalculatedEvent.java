package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentBaselineCalculatedEvent {

    private Long equipmentId;
    private Long evaluationPeriodId;
    private Long algorithmVersionId;
    private String periodType;
    private BigDecimal equipmentStandardPerformanceRate;
    private BigDecimal equipmentBaselineErrorRate;
    private BigDecimal equipmentEtaAge;
    private BigDecimal equipmentEtaMaint;
    private Integer equipmentAgeMonths;
    private BigDecimal equipmentIdx;
    private String currentEquipmentGrade;
    private LocalDateTime calculatedAt;
}
