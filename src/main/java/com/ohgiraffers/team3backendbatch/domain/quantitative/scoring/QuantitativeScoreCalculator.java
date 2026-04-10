package com.ohgiraffers.team3backendbatch.domain.quantitative.scoring;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class QuantitativeScoreCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TEN = BigDecimal.TEN;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3600);
    private static final BigDecimal EPSILON = BigDecimal.valueOf(0.0001);

    private static final BigDecimal UPH_WEIGHT = BigDecimal.valueOf(0.35);
    private static final BigDecimal YIELD_WEIGHT = BigDecimal.valueOf(0.45);
    private static final BigDecimal LEAD_TIME_WEIGHT = BigDecimal.valueOf(0.20);

    private static final BigDecimal DEFAULT_TEMP_WEIGHT = BigDecimal.valueOf(0.40);
    private static final BigDecimal DEFAULT_HUMIDITY_WEIGHT = BigDecimal.valueOf(0.30);
    private static final BigDecimal DEFAULT_PARTICLE_WEIGHT = BigDecimal.valueOf(0.30);
    private static final BigDecimal DEFAULT_LOT_THRESHOLD = BigDecimal.valueOf(0.60);

    private static final BigDecimal AGE_DECAY_LAMBDA = BigDecimal.valueOf(1.00);
    private static final BigDecimal MAINT_DECAY_LAMBDA = BigDecimal.valueOf(1.20);
    private static final BigDecimal AGE_FACTOR = BigDecimal.valueOf(0.12);
    private static final BigDecimal MAINT_FACTOR = BigDecimal.valueOf(0.08);
    private static final BigDecimal ENV_FACTOR = BigDecimal.valueOf(0.05);
    private static final BigDecimal MATERIAL_FACTOR = BigDecimal.valueOf(0.10);
    private static final BigDecimal EIDX_MAX = BigDecimal.valueOf(1.30);
    private static final BigDecimal BASELINE_AGE_FACTOR = BigDecimal.valueOf(0.50);
    private static final BigDecimal SHIELDING_RELIEF = BigDecimal.valueOf(0.30);
    private static final BigDecimal CHALLENGE_BONUS_SCALE = BigDecimal.valueOf(50);
    private static final BigDecimal CHALLENGE_BONUS_CAP = BigDecimal.valueOf(20);

    public BigDecimal resolveActualError(BigDecimal actualError, BigDecimal totalDefectQty, BigDecimal totalInputQty) {
        if (isPositive(actualError)) {
            return scale(actualError);
        }
        if (!isPositive(totalInputQty) || totalDefectQty == null) {
            return ZERO;
        }
        return scale(totalDefectQty.multiply(HUNDRED).divide(totalInputQty, 4, RoundingMode.HALF_UP));
    }

    public BigDecimal calculateUphScore(BigDecimal averageLeadTimeSec, BigDecimal targetUph) {
        if (!isPositive(averageLeadTimeSec)) {
            return ZERO;
        }
        BigDecimal actualUph = SECONDS_PER_HOUR.divide(averageLeadTimeSec, 4, RoundingMode.HALF_UP);
        return calculateRatioScore(actualUph, targetUph);
    }

    public BigDecimal calculateYieldScore(BigDecimal totalGoodQty, BigDecimal totalInputQty, BigDecimal targetYieldRate) {
        if (!isPositive(totalInputQty) || totalGoodQty == null) {
            return ZERO;
        }
        BigDecimal actualYieldRate = totalGoodQty.multiply(HUNDRED).divide(totalInputQty, 4, RoundingMode.HALF_UP);
        return calculateRatioScore(actualYieldRate, targetYieldRate);
    }

    public BigDecimal calculateLeadTimeScore(BigDecimal averageLeadTimeSec, BigDecimal targetLeadTimeSec) {
        if (!isPositive(averageLeadTimeSec)) {
            return ZERO;
        }
        return calculateInverseRatioScore(averageLeadTimeSec, targetLeadTimeSec);
    }

    public BigDecimal calculateNAge(
        LocalDate equipmentInstallDate,
        LocalDate evaluationPeriodEndDate,
        Integer equipmentWarrantyMonths,
        Integer equipmentDesignLifeMonths
    ) {
        if (equipmentInstallDate == null || evaluationPeriodEndDate == null) {
            return ZERO;
        }
        if (equipmentWarrantyMonths == null || equipmentDesignLifeMonths == null) {
            return ZERO;
        }

        long ageMonths = Math.max(0, ChronoUnit.MONTHS.between(
            YearMonth.from(equipmentInstallDate),
            YearMonth.from(evaluationPeriodEndDate)
        ));
        int effectiveLife = Math.max(equipmentDesignLifeMonths - equipmentWarrantyMonths, 1);
        BigDecimal normalized = BigDecimal.valueOf(ageMonths - equipmentWarrantyMonths)
            .divide(BigDecimal.valueOf(effectiveLife), 4, RoundingMode.HALF_UP);
        return clampRatio(normalized);
    }

    public BigDecimal calculateEtaAge(BigDecimal equipmentWearCoefficient, BigDecimal nAge) {
        if (equipmentWearCoefficient == null || nAge == null) {
            return scale(ONE);
        }
        double exponent = -AGE_DECAY_LAMBDA.doubleValue()
            * safeRatio(equipmentWearCoefficient).doubleValue()
            * safeRatio(nAge).doubleValue();
        BigDecimal etaAge = BigDecimal.valueOf(Math.exp(exponent));
        return clampRatio(etaAge);
    }

    public BigDecimal calculateNMaint(BigDecimal maintenanceWeightedScoreSum, BigDecimal maintenanceWeightSum) {
        if (!isPositive(maintenanceWeightSum)) {
            return scale(ONE);
        }
        BigDecimal normalized = safe(maintenanceWeightedScoreSum)
            .divide(maintenanceWeightSum, 4, RoundingMode.HALF_UP)
            .divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return clampRatio(normalized);
    }

    public BigDecimal calculateEtaMaint(BigDecimal maintenanceScoreNorm) {
        if (maintenanceScoreNorm == null) {
            return scale(ONE);
        }
        BigDecimal normalized = clampRatio(maintenanceScoreNorm);
        double exponent = -MAINT_DECAY_LAMBDA.doubleValue() * ONE.subtract(normalized).doubleValue();
        BigDecimal etaMaint = BigDecimal.valueOf(Math.exp(exponent));
        return clampRatio(etaMaint);
    }

    public BigDecimal calculateNEnv(
        BigDecimal environmentTemperature,
        BigDecimal environmentTempMin,
        BigDecimal environmentTempMax,
        BigDecimal environmentHumidity,
        BigDecimal environmentHumidityMin,
        BigDecimal environmentHumidityMax,
        BigDecimal environmentParticleCount,
        BigDecimal environmentParticleLimit,
        BigDecimal environmentTempWeight,
        BigDecimal environmentHumidityWeight,
        BigDecimal environmentParticleWeight
    ) {
        BigDecimal vTemp = calculateRangeDeviation(environmentTemperature, environmentTempMin, environmentTempMax);
        BigDecimal vHumidity = calculateRangeDeviation(environmentHumidity, environmentHumidityMin, environmentHumidityMax);
        BigDecimal vParticle = calculateParticleDeviation(environmentParticleCount, environmentParticleLimit);

        BigDecimal tempWeight = positiveOrDefault(environmentTempWeight, DEFAULT_TEMP_WEIGHT);
        BigDecimal humidityWeight = positiveOrDefault(environmentHumidityWeight, DEFAULT_HUMIDITY_WEIGHT);
        BigDecimal particleWeight = positiveOrDefault(environmentParticleWeight, DEFAULT_PARTICLE_WEIGHT);
        BigDecimal weightSum = tempWeight.add(humidityWeight).add(particleWeight);

        if (!isPositive(weightSum)) {
            return ZERO;
        }

        BigDecimal normalized = vTemp.multiply(tempWeight)
            .add(vHumidity.multiply(humidityWeight))
            .add(vParticle.multiply(particleWeight))
            .divide(weightSum, 4, RoundingMode.HALF_UP);

        return clampRatio(normalized);
    }

    public BigDecimal calculateMaterialShielding(
        Integer defectiveWorkersSameLot,
        Integer totalWorkersSameLot,
        BigDecimal lotDefectThreshold
    ) {
        if (defectiveWorkersSameLot == null || totalWorkersSameLot == null || totalWorkersSameLot <= 0) {
            return ZERO;
        }
        BigDecimal threshold = positiveOrDefault(lotDefectThreshold, DEFAULT_LOT_THRESHOLD);
        BigDecimal spikeRatio = BigDecimal.valueOf(defectiveWorkersSameLot)
            .divide(BigDecimal.valueOf(Math.max(totalWorkersSameLot, 1)), 4, RoundingMode.HALF_UP);
        return spikeRatio.compareTo(threshold) >= 0 ? scale(ONE) : ZERO;
    }

    public BigDecimal calculateEIdx(
        String equipmentGrade,
        BigDecimal nAge,
        BigDecimal etaAge,
        BigDecimal etaMaint,
        BigDecimal nEnv,
        BigDecimal materialShielding
    ) {
        if (isProtectedGrade(equipmentGrade) || safeRatio(nAge).compareTo(BigDecimal.ZERO) <= 0) {
            return scale(ONE);
        }

        BigDecimal eIdx = ONE
            .add(AGE_FACTOR.multiply(ONE.subtract(safeRatio(etaAge))))
            .add(MAINT_FACTOR.multiply(ONE.subtract(safeRatio(etaMaint))))
            .add(ENV_FACTOR.multiply(safeRatio(nEnv)))
            .add(MATERIAL_FACTOR.multiply(safeRatio(materialShielding)));

        return scale(eIdx.max(ONE).min(EIDX_MAX));
    }

    public BigDecimal calculateDifficultyAdjustment(BigDecimal difficultyScore) {
        return calculateDifficultyAdjustment(difficultyScore, null);
    }

    public BigDecimal calculateDifficultyAdjustment(BigDecimal difficultyScore, String difficultyGrade) {
        BigDecimal difficultyLevel = resolveDifficultyLevel(difficultyScore, difficultyGrade);
        if (difficultyLevel == null) {
            return scale(ONE);
        }
        return scale(BigDecimal.valueOf(0.90).add(difficultyLevel.multiply(BigDecimal.valueOf(0.05))));
    }

    public BigDecimal calculateBaselineError(BigDecimal baselineError, BigDecimal errorReferenceRate, BigDecimal nAge) {
        if (isPositive(baselineError)) {
            return scale(baselineError);
        }
        if (!isPositive(errorReferenceRate)) {
            return ZERO;
        }
        BigDecimal calculated = errorReferenceRate.multiply(ONE.add(BASELINE_AGE_FACTOR.multiply(safeRatio(nAge))));
        return scale(calculated);
    }

    public BigDecimal calculateAdjustedBaselineError(BigDecimal baselineError, BigDecimal eIdx) {
        if (!isPositive(baselineError)) {
            return ZERO;
        }
        return scale(baselineError.multiply(positiveOrDefault(eIdx, ONE)));
    }

    public BigDecimal calculateQBase(BigDecimal uphScore, BigDecimal yieldScore, BigDecimal leadTimeScore) {
        BigDecimal qBase = safe(uphScore).multiply(UPH_WEIGHT)
            .add(safe(yieldScore).multiply(YIELD_WEIGHT))
            .add(safe(leadTimeScore).multiply(LEAD_TIME_WEIGHT));
        return clampPercentage(qBase);
    }

    public BigDecimal calculateEffectiveActualError(BigDecimal actualError, BigDecimal materialShielding) {
        if (!isPositive(actualError)) {
            return ZERO;
        }
        BigDecimal effectiveError = actualError.multiply(ONE.subtract(SHIELDING_RELIEF.multiply(safeRatio(materialShielding))));
        return scale(effectiveError.max(BigDecimal.ZERO));
    }

    public BigDecimal calculateBonusPoint(BigDecimal difficultyScore, String difficultyGrade, String currentSkillTier) {
        BigDecimal difficultyCapability = resolveDifficultyCapability(difficultyScore, difficultyGrade);
        BigDecimal workerCapability = resolveWorkerCapability(currentSkillTier);

        if (!isPositive(difficultyCapability) || !isPositive(workerCapability)) {
            return ZERO;
        }

        BigDecimal capabilityGap = difficultyCapability.subtract(workerCapability);
        if (capabilityGap.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }

        return scale(capabilityGap.multiply(CHALLENGE_BONUS_SCALE).min(CHALLENGE_BONUS_CAP));
    }

    public BigDecimal calculateProvisionalSQuantFromErrorRate(
        BigDecimal effectiveActualError,
        BigDecimal adjustedBaselineError,
        BigDecimal difficultyAdjustment,
        BigDecimal bonusPoint,
        BigDecimal fallbackBaseScore
    ) {
        BigDecimal normalizedDifficulty = positiveOrDefault(difficultyAdjustment, ONE);

        if (!isPositive(adjustedBaselineError)) {
            BigDecimal fallback = safe(fallbackBaseScore).add(safe(bonusPoint));
            return clampPercentage(fallback);
        }

        BigDecimal errorImprovementScore = adjustedBaselineError.subtract(safe(effectiveActualError))
            .divide(adjustedBaselineError.max(EPSILON), 4, RoundingMode.HALF_UP)
            .multiply(HUNDRED);

        BigDecimal raw = errorImprovementScore
            .multiply(normalizedDifficulty)
            .add(safe(bonusPoint));

        return clampPercentage(raw);
    }

    public BigDecimal resolveMonthlyCorrection(BigDecimal correction, BatchPeriodType periodType) {
        if (periodType != BatchPeriodType.MONTH || correction == null) {
            return ZERO;
        }
        return scale(correction);
    }

    public BigDecimal resolveMonthlyPenalty(BigDecimal penalty, BatchPeriodType periodType) {
        if (periodType != BatchPeriodType.MONTH || penalty == null) {
            return ZERO;
        }
        return scale(penalty.max(BigDecimal.ZERO));
    }

    public BigDecimal calculateFinalSQuant(
        BigDecimal provisionalSQuant,
        BigDecimal environmentCorrection,
        BigDecimal materialCorrection,
        BigDecimal antiGamingPenalty,
        BatchPeriodType periodType
    ) {
        if (periodType != BatchPeriodType.MONTH) {
            return clampPercentage(provisionalSQuant);
        }

        BigDecimal finalScore = safe(provisionalSQuant)
            .add(safe(environmentCorrection))
            .add(safe(materialCorrection))
            .subtract(safe(antiGamingPenalty));
        return clampPercentage(finalScore);
    }

    public BigDecimal calculateTScore(
        BigDecimal sQuant,
        BigDecimal groupMean,
        BigDecimal groupStdDev,
        BatchPeriodType periodType
    ) {
        if (periodType != BatchPeriodType.MONTH) {
            return null;
        }
        if (!isPositive(groupStdDev) || groupMean == null) {
            return scale(sQuant);
        }
        BigDecimal tScore = BigDecimal.valueOf(50).add(
            TEN.multiply(
                safe(sQuant).subtract(groupMean)
                    .divide(groupStdDev.max(EPSILON), 4, RoundingMode.HALF_UP)
            )
        );
        return clampPercentage(tScore);
    }

    public String resolveStatus(BatchPeriodType periodType) {
        return periodType == BatchPeriodType.MONTH ? "SETTLED" : "PREVIEW";
    }

    private BigDecimal calculateRatioScore(BigDecimal actual, BigDecimal target) {
        if (!isPositive(actual)) {
            return ZERO;
        }
        if (!isPositive(target)) {
            return clampPercentage(actual);
        }
        return clampPercentage(actual.multiply(HUNDRED).divide(target, 4, RoundingMode.HALF_UP));
    }

    private BigDecimal calculateInverseRatioScore(BigDecimal actual, BigDecimal target) {
        if (!isPositive(actual) || !isPositive(target)) {
            return ZERO;
        }
        return clampPercentage(target.multiply(HUNDRED).divide(actual, 4, RoundingMode.HALF_UP));
    }

    private BigDecimal calculateRangeDeviation(BigDecimal actual, BigDecimal min, BigDecimal max) {
        if (actual == null || min == null || max == null || max.compareTo(min) <= 0) {
            return ZERO;
        }
        BigDecimal deviation = BigDecimal.ZERO;
        if (actual.compareTo(min) < 0) {
            deviation = min.subtract(actual);
        } else if (actual.compareTo(max) > 0) {
            deviation = actual.subtract(max);
        }
        BigDecimal range = max.subtract(min);
        if (!isPositive(range)) {
            return ZERO;
        }
        return clampRatio(deviation.divide(range, 4, RoundingMode.HALF_UP));
    }

    private BigDecimal calculateParticleDeviation(BigDecimal actual, BigDecimal limit) {
        if (actual == null || !isPositive(limit)) {
            return ZERO;
        }
        BigDecimal deviation = actual.subtract(limit);
        if (deviation.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }
        return clampRatio(deviation.divide(limit, 4, RoundingMode.HALF_UP));
    }

    private boolean isProtectedGrade(String equipmentGrade) {
        if (equipmentGrade == null) {
            return false;
        }
        return "S".equalsIgnoreCase(equipmentGrade) || "A".equalsIgnoreCase(equipmentGrade);
    }

    private BigDecimal resolveDifficultyLevel(BigDecimal difficultyScore, String difficultyGrade) {
        if (difficultyGrade != null && !difficultyGrade.isBlank()) {
            return switch (difficultyGrade.trim().toUpperCase()) {
                case "D5" -> BigDecimal.valueOf(5);
                case "D4" -> BigDecimal.valueOf(4);
                case "D3" -> BigDecimal.valueOf(3);
                case "D2" -> BigDecimal.valueOf(2);
                case "D1" -> BigDecimal.ONE;
                default -> null;
            };
        }

        if (difficultyScore == null) {
            return null;
        }

        if (difficultyScore.compareTo(BigDecimal.valueOf(5)) <= 0) {
            return difficultyScore;
        }
        if (difficultyScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return BigDecimal.valueOf(5);
        }
        if (difficultyScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return BigDecimal.valueOf(4);
        }
        if (difficultyScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return BigDecimal.valueOf(3);
        }
        if (difficultyScore.compareTo(BigDecimal.valueOf(30)) >= 0) {
            return BigDecimal.valueOf(2);
        }
        return BigDecimal.ONE;
    }

    private BigDecimal resolveDifficultyCapability(BigDecimal difficultyScore, String difficultyGrade) {
        BigDecimal difficultyLevel = resolveDifficultyLevel(difficultyScore, difficultyGrade);
        if (difficultyLevel == null) {
            return null;
        }

        return switch (difficultyLevel.intValue()) {
            case 5 -> scale(ONE);
            case 4 -> scale(BigDecimal.valueOf(0.90));
            case 3 -> scale(BigDecimal.valueOf(0.80));
            case 2 -> scale(BigDecimal.valueOf(0.70));
            default -> scale(BigDecimal.valueOf(0.60));
        };
    }

    private BigDecimal resolveWorkerCapability(String currentSkillTier) {
        if (currentSkillTier == null || currentSkillTier.isBlank()) {
            return null;
        }

        return switch (currentSkillTier.trim().toUpperCase()) {
            case "S" -> scale(ONE);
            case "A" -> scale(BigDecimal.valueOf(0.90));
            case "B" -> scale(BigDecimal.valueOf(0.80));
            case "C" -> scale(BigDecimal.valueOf(0.70));
            default -> null;
        };
    }

    private BigDecimal clampPercentage(BigDecimal value) {
        BigDecimal clamped = safe(value).max(BigDecimal.ZERO).min(HUNDRED);
        return scale(clamped);
    }

    private BigDecimal clampRatio(BigDecimal value) {
        BigDecimal clamped = safe(value).max(BigDecimal.ZERO).min(ONE);
        return scale(clamped);
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal positiveOrDefault(BigDecimal value, BigDecimal defaultValue) {
        return isPositive(value) ? value : defaultValue;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal safeRatio(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scale(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }
}
