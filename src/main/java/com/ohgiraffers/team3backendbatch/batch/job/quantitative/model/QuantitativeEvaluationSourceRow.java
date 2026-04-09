package com.ohgiraffers.team3backendbatch.batch.job.quantitative.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuantitativeEvaluationSourceRow {

    private Long quantitativeEvaluationId;
    private Long employeeId;
    private Long evaluationPeriodId;
    private Long equipmentId;
    private Long algorithmVersionId;
    private LocalDate evaluationPeriodEndDate;
    private BigDecimal totalInputQty;
    private BigDecimal totalGoodQty;
    private BigDecimal totalDefectQty;
    private BigDecimal averageLeadTimeSec;
    private BigDecimal downtimeMinutes;
    private BigDecimal maintenanceMinutes;
    private BigDecimal targetUph;
    private BigDecimal targetYieldRate;
    private BigDecimal targetLeadTimeSec;
    private LocalDate equipmentInstallDate;
    private String equipmentGrade;
    private Integer equipmentWarrantyMonths;
    private Integer equipmentDesignLifeMonths;
    private BigDecimal equipmentWearCoefficient;
    private BigDecimal maintenanceWeightedScoreSum;
    private BigDecimal maintenanceWeightSum;
    private BigDecimal environmentTemperature;
    private BigDecimal environmentHumidity;
    private BigDecimal environmentParticleCount;
    private BigDecimal environmentTempMin;
    private BigDecimal environmentTempMax;
    private BigDecimal environmentHumidityMin;
    private BigDecimal environmentHumidityMax;
    private BigDecimal environmentParticleLimit;
    private BigDecimal environmentTempWeight;
    private BigDecimal environmentHumidityWeight;
    private BigDecimal environmentParticleWeight;
    private Integer defectiveWorkersSameLot;
    private Integer totalWorkersSameLot;
    private BigDecimal lotDefectThreshold;
    private BigDecimal difficultyScore;
    private String difficultyGrade;
    private BigDecimal errorReferenceRate;
    private BigDecimal baselineError;
    private BigDecimal environmentCorrection;
    private BigDecimal materialCorrection;
    private BigDecimal antiGamingPenalty;
    private BigDecimal groupMean;
    private BigDecimal groupStdDev;
    private BigDecimal actualError;
}