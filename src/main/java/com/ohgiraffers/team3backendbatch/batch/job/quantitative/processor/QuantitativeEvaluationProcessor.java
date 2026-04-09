package com.ohgiraffers.team3backendbatch.batch.job.quantitative.processor;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeCalculationResult;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.domain.quantitative.scoring.QuantitativeScoreCalculator;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuantitativeEvaluationProcessor
    implements ItemProcessor<QuantitativeEvaluationAggregate, QuantitativeEvaluationAggregate> {

    private final QuantitativeScoreCalculator quantitativeScoreCalculator;

    @Override
    public QuantitativeEvaluationAggregate process(QuantitativeEvaluationAggregate item) {
        BigDecimal resolvedActualError = quantitativeScoreCalculator.resolveActualError(
            item.getActualError(),
            item.getTotalDefectQty(),
            item.getTotalInputQty()
        );
        BigDecimal uphScore = quantitativeScoreCalculator.calculateUphScore(item.getAverageLeadTimeSec(), item.getTargetUph());
        BigDecimal yieldScore = quantitativeScoreCalculator.calculateYieldScore(
            item.getTotalGoodQty(),
            item.getTotalInputQty(),
            item.getTargetYieldRate()
        );
        BigDecimal leadTimeScore = quantitativeScoreCalculator.calculateLeadTimeScore(
            item.getAverageLeadTimeSec(),
            item.getTargetLeadTimeSec()
        );

        BigDecimal nAge = quantitativeScoreCalculator.calculateNAge(
            item.getEquipmentInstallDate(),
            item.getEvaluationPeriodEndDate(),
            item.getEquipmentWarrantyMonths(),
            item.getEquipmentDesignLifeMonths()
        );
        BigDecimal etaAge = quantitativeScoreCalculator.calculateEtaAge(item.getEquipmentWearCoefficient(), nAge);
        BigDecimal nMaint = quantitativeScoreCalculator.calculateNMaint(
            item.getMaintenanceWeightedScoreSum(),
            item.getMaintenanceWeightSum()
        );
        BigDecimal nEnv = quantitativeScoreCalculator.calculateNEnv(
            item.getEnvironmentTemperature(),
            item.getEnvironmentTempMin(),
            item.getEnvironmentTempMax(),
            item.getEnvironmentHumidity(),
            item.getEnvironmentHumidityMin(),
            item.getEnvironmentHumidityMax(),
            item.getEnvironmentParticleCount(),
            item.getEnvironmentParticleLimit(),
            item.getEnvironmentTempWeight(),
            item.getEnvironmentHumidityWeight(),
            item.getEnvironmentParticleWeight()
        );
        BigDecimal materialShielding = quantitativeScoreCalculator.calculateMaterialShielding(
            item.getDefectiveWorkersSameLot(),
            item.getTotalWorkersSameLot(),
            item.getLotDefectThreshold()
        );
        BigDecimal difficultyAdjustment = quantitativeScoreCalculator.calculateDifficultyAdjustment(
            item.getDifficultyScore(),
            item.getDifficultyGrade()
        );
        BigDecimal resolvedBaselineError = quantitativeScoreCalculator.calculateBaselineError(
            item.getBaselineError(),
            item.getErrorReferenceRate(),
            nAge
        );
        BigDecimal qBase = quantitativeScoreCalculator.calculateQBase(uphScore, yieldScore, leadTimeScore);
        BigDecimal eIdx = quantitativeScoreCalculator.calculateEIdx(
            item.getEquipmentGrade(),
            nAge,
            etaAge,
            nMaint,
            nEnv,
            materialShielding
        );
        BigDecimal pError = quantitativeScoreCalculator.calculatePError(
            resolvedActualError,
            resolvedBaselineError,
            materialShielding
        );
        BigDecimal bonusPoint = quantitativeScoreCalculator.calculateBonusPoint(
            item.getDifficultyScore(),
            item.getDifficultyGrade(),
            item.getCurrentSkillTier()
        );
        BigDecimal provisionalSQuant = quantitativeScoreCalculator.calculateProvisionalSQuantFromErrorRate(
            resolvedActualError,
            resolvedBaselineError,
            difficultyAdjustment,
            bonusPoint,
            qBase
        );
        BigDecimal environmentCorrection = quantitativeScoreCalculator.resolveMonthlyCorrection(
            item.getEnvironmentCorrection(),
            item.getPeriodType()
        );
        BigDecimal materialCorrection = quantitativeScoreCalculator.resolveMonthlyCorrection(
            item.getMaterialCorrection(),
            item.getPeriodType()
        );
        BigDecimal antiGamingPenalty = quantitativeScoreCalculator.resolveMonthlyPenalty(
            item.getAntiGamingPenalty(),
            item.getPeriodType()
        );
        BigDecimal sQuant = quantitativeScoreCalculator.calculateFinalSQuant(
            provisionalSQuant,
            environmentCorrection,
            materialCorrection,
            antiGamingPenalty,
            item.getPeriodType()
        );
        BigDecimal tScore = quantitativeScoreCalculator.calculateTScore(
            sQuant,
            item.getGroupMean(),
            item.getGroupStdDev(),
            item.getPeriodType()
        );
        String status = quantitativeScoreCalculator.resolveStatus(item.getPeriodType());

        QuantitativeCalculationResult result = QuantitativeCalculationResult.builder()
            .actualError(resolvedActualError)
            .nAge(nAge)
            .etaAge(etaAge)
            .nMaint(nMaint)
            .nEnv(nEnv)
            .materialShielding(materialShielding)
            .uphScore(uphScore)
            .yieldScore(yieldScore)
            .leadTimeScore(leadTimeScore)
            .difficultyAdjustment(difficultyAdjustment)
            .baselineError(resolvedBaselineError)
            .qBase(qBase)
            .eIdx(eIdx)
            .bonusPoint(bonusPoint)
            .provisionalSQuant(provisionalSQuant)
            .environmentCorrection(environmentCorrection)
            .materialCorrection(materialCorrection)
            .antiGamingPenalty(antiGamingPenalty)
            .sQuant(sQuant)
            .tScore(tScore)
            .status(status)
            .build();

        return item.withCalculatedResult(result);
    }
}
