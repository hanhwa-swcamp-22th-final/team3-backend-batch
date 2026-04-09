package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentReferenceSnapshotEvent {
    private Long equipmentId;
    private String equipmentCode;
    private String equipmentStatus;
    private String equipmentGrade;
    private LocalDateTime equipmentInstallDate;
    private Long environmentStandardId;
    private Integer equipmentWarrantyMonth;
    private Integer equipmentDesignLifeMonths;
    private BigDecimal equipmentWearCoefficient;
    private BigDecimal equipmentStandardPerformanceRate;
    private BigDecimal equipmentBaselineErrorRate;
    private BigDecimal equipmentEtaMaint;
    private BigDecimal equipmentIdx;
    private BigDecimal envTempMin;
    private BigDecimal envTempMax;
    private BigDecimal envHumidityMin;
    private BigDecimal envHumidityMax;
    private Integer envParticleLimit;
    private Boolean deleted;
    private LocalDateTime occurredAt;
}
