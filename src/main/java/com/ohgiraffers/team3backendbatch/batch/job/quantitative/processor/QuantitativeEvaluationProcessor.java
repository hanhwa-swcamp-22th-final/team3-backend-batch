package com.ohgiraffers.team3backendbatch.batch.job.quantitative.processor;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeCalculationResult;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.domain.quantitative.scoring.QuantitativeScoreCalculator;
import com.ohgiraffers.team3backendbatch.domain.quantitative.scoring.QuantitativeScoringPolicy;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuantitativeEvaluationProcessor
    implements ItemProcessor<QuantitativeEvaluationAggregate, QuantitativeEvaluationAggregate> {

    private final QuantitativeScoreCalculator quantitativeScoreCalculator;

    /**
     * 정량 평가 집계 데이터로부터 계산 결과를 생성한다.
     * @param item 정량 평가 집계 데이터
     * @return 계산 결과가 반영된 정량 평가 집계 데이터
     */
    @Override
    public QuantitativeEvaluationAggregate process(QuantitativeEvaluationAggregate item) {
        QuantitativeScoringPolicy policy = quantitativeScoreCalculator.resolvePolicy(item.getPolicyConfig());
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
        Integer equipmentAgeMonths = quantitativeScoreCalculator.calculateEquipmentAgeMonths(
            item.getEquipmentInstallDate(),
            item.getEvaluationPeriodEndDate()
        );
        BigDecimal etaAge = quantitativeScoreCalculator.calculateEtaAge(item.getEquipmentWearCoefficient(), nAge, policy);
        BigDecimal nMaint = quantitativeScoreCalculator.calculateNMaint(
            item.getMaintenanceWeightedScoreSum(),
            item.getMaintenanceWeightSum()
        );
        BigDecimal etaMaint = quantitativeScoreCalculator.calculateEtaMaint(nMaint, policy);
        BigDecimal nEnv = quantitativeScoreCalculator.resolveNEnv(
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
            item.getEnvironmentParticleWeight(),
            item.getUnresolvedEnvironmentEventCount(),
            policy
        );
        BigDecimal materialShielding = quantitativeScoreCalculator.resolveMaterialShielding(
            item.getFailedLotCount(),
            item.getTotalLotCount(),
            item.getPeerLotFailRate(),
            item.getLotDefectThreshold(),
            item.getPeriodType(),
            policy
        );
        BigDecimal difficultyAdjustment = quantitativeScoreCalculator.calculateDifficultyAdjustment(
            item.getDifficultyScore(),
            item.getDifficultyGrade()
        );
        BigDecimal resolvedBaselineError = quantitativeScoreCalculator.calculateBaselineError(
            item.getBaselineError(),
            item.getErrorReferenceRate(),
            nAge,
            policy
        );
        BigDecimal qBase = quantitativeScoreCalculator.calculateQBase(uphScore, yieldScore, leadTimeScore, policy);
        String currentEquipmentGrade = quantitativeScoreCalculator.resolveCurrentEquipmentGrade(
            item.getEquipmentGrade(),
            nAge,
            policy
        );
        BigDecimal eIdx = quantitativeScoreCalculator.calculateEIdx(
            currentEquipmentGrade,
            nAge,
            etaAge,
            etaMaint,
            policy
        );
        BigDecimal currentEquipmentIdx = eIdx;
        BigDecimal adjustedBaselineError = quantitativeScoreCalculator.calculateEnvironmentAdjustedBaselineError(
            resolvedBaselineError,
            eIdx,
            nEnv,
            policy
        );
        BigDecimal effectiveActualError = quantitativeScoreCalculator.calculateEffectiveActualError(
            resolvedActualError,
            materialShielding,
            policy
        );
        BigDecimal bonusPoint = quantitativeScoreCalculator.calculateBonusPoint(
            item.getDifficultyScore(),
            item.getDifficultyGrade(),
            item.getCurrentSkillTier(),
            policy
        );
        BigDecimal provisionalSQuant = quantitativeScoreCalculator.calculateProvisionalSQuantFromErrorRate(
            effectiveActualError,
            adjustedBaselineError,
            difficultyAdjustment,
            bonusPoint,
            qBase
        );
        BigDecimal antiGamingPenalty = quantitativeScoreCalculator.resolveMonthlyPenalty(
            item.getAntiGamingPenalty(),
            item.getPeriodType()
        );
        BigDecimal sQuant = quantitativeScoreCalculator.calculateFinalSQuant(
            provisionalSQuant,
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
            .equipmentAgeMonths(equipmentAgeMonths)
            .nMaint(nMaint)
            .etaMaint(etaMaint)
            .nEnv(nEnv)
            .materialShielding(materialShielding)
            .uphScore(uphScore)
            .yieldScore(yieldScore)
            .leadTimeScore(leadTimeScore)
            .difficultyAdjustment(difficultyAdjustment)
            .baselineError(resolvedBaselineError)
            .qBase(qBase)
            .eIdx(eIdx)
            .currentEquipmentIdx(currentEquipmentIdx)
            .currentEquipmentGrade(currentEquipmentGrade)
            .bonusPoint(bonusPoint)
            .provisionalSQuant(provisionalSQuant)
            .antiGamingPenalty(antiGamingPenalty)
            .sQuant(sQuant)
            .tScore(tScore)
            .status(status)
            .build();

        return item.withCalculatedResult(result);
    }
}
