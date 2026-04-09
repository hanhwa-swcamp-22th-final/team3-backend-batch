package com.ohgiraffers.team3backendbatch.batch.job.quantitative.model;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Quantitative evaluation input and computed result model.
 *
 * The reader builds this object from raw MES/admin reference aggregates.
 * The processor enriches it with provisional metrics and settled score fields.
 * The writer publishes the final equipment score payload to HR.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class QuantitativeEvaluationAggregate {

    private final Long quantitativeEvaluationId;
    private final Long employeeId;
    private final Long evaluationPeriodId;
    private final Long equipmentId;
    private final BatchPeriodType periodType;
    private final Long algorithmVersionId;
    private final LocalDate evaluationPeriodEndDate;
    private final BigDecimal totalInputQty;
    private final BigDecimal totalGoodQty;
    private final BigDecimal totalDefectQty;
    private final BigDecimal averageLeadTimeSec;
    private final BigDecimal downtimeMinutes;
    private final BigDecimal maintenanceMinutes;
    private final BigDecimal targetUph;
    private final BigDecimal targetYieldRate;
    private final BigDecimal targetLeadTimeSec;

    private final LocalDate equipmentInstallDate;
    private final String equipmentGrade;
    private final Integer equipmentWarrantyMonths;
    private final Integer equipmentDesignLifeMonths;
    private final BigDecimal equipmentWearCoefficient;

    private final BigDecimal maintenanceWeightedScoreSum;
    private final BigDecimal maintenanceWeightSum;

    private final BigDecimal environmentTemperature;
    private final BigDecimal environmentHumidity;
    private final BigDecimal environmentParticleCount;
    private final BigDecimal environmentTempMin;
    private final BigDecimal environmentTempMax;
    private final BigDecimal environmentHumidityMin;
    private final BigDecimal environmentHumidityMax;
    private final BigDecimal environmentParticleLimit;
    private final BigDecimal environmentTempWeight;
    private final BigDecimal environmentHumidityWeight;
    private final BigDecimal environmentParticleWeight;

    private final Integer defectiveWorkersSameLot;
    private final Integer totalWorkersSameLot;
    private final BigDecimal lotDefectThreshold;

    private final BigDecimal difficultyScore;
    private final String difficultyGrade;
    private final String currentSkillTier;
    private final BigDecimal errorReferenceRate;
    private final BigDecimal baselineError;
    private final BigDecimal environmentCorrection;
    private final BigDecimal materialCorrection;
    private final BigDecimal antiGamingPenalty;
    private final BigDecimal groupMean;
    private final BigDecimal groupStdDev;

    private final BigDecimal actualError;
    private final BigDecimal nAge;
    private final BigDecimal etaAge;
    private final BigDecimal nMaint;
    private final BigDecimal nEnv;
    private final BigDecimal materialShielding;
    private final BigDecimal uphScore;
    private final BigDecimal yieldScore;
    private final BigDecimal leadTimeScore;
    private final BigDecimal difficultyAdjustment;
    private final BigDecimal qBase;
    private final BigDecimal eIdx;
    private final BigDecimal bonusPoint;
    private final BigDecimal provisionalSQuant;
    private final BigDecimal sQuant;
    private final BigDecimal tScore;
    private final String status;

    public QuantitativeEvaluationAggregate withCalculatedResult(QuantitativeCalculationResult result) {
        return this.toBuilder()
            .actualError(result.getActualError())
            .nAge(result.getNAge())
            .etaAge(result.getEtaAge())
            .nMaint(result.getNMaint())
            .nEnv(result.getNEnv())
            .materialShielding(result.getMaterialShielding())
            .uphScore(result.getUphScore())
            .yieldScore(result.getYieldScore())
            .leadTimeScore(result.getLeadTimeScore())
            .difficultyAdjustment(result.getDifficultyAdjustment())
            .baselineError(result.getBaselineError())
            .qBase(result.getQBase())
            .eIdx(result.getEIdx())
            .bonusPoint(result.getBonusPoint())
            .provisionalSQuant(result.getProvisionalSQuant())
            .environmentCorrection(result.getEnvironmentCorrection())
            .materialCorrection(result.getMaterialCorrection())
            .antiGamingPenalty(result.getAntiGamingPenalty())
            .sQuant(result.getSQuant())
            .tScore(result.getTScore())
            .status(result.getStatus())
            .build();
    }
}
