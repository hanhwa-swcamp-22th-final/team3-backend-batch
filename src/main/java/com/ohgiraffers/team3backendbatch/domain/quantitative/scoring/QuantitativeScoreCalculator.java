package com.ohgiraffers.team3backendbatch.domain.quantitative.scoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final BigDecimal AGE_DECAY_LAMBDA = BigDecimal.valueOf(2.00);
    private static final BigDecimal MIN_ETA_AGE = BigDecimal.valueOf(0.30);
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
    private static final BigDecimal GRADE_BOUNDARY_A = BigDecimal.valueOf(0.33);
    private static final BigDecimal GRADE_BOUNDARY_B = BigDecimal.valueOf(0.66);

    private final ObjectMapper objectMapper;
    private final QuantitativeScoringPolicy defaultPolicy;

    public QuantitativeScoreCalculator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.defaultPolicy = QuantitativeScoringPolicy.builder()
            .uphWeight(UPH_WEIGHT)
            .yieldWeight(YIELD_WEIGHT)
            .leadTimeWeight(LEAD_TIME_WEIGHT)
            .defaultTempWeight(DEFAULT_TEMP_WEIGHT)
            .defaultHumidityWeight(DEFAULT_HUMIDITY_WEIGHT)
            .defaultParticleWeight(DEFAULT_PARTICLE_WEIGHT)
            .defaultLotThreshold(DEFAULT_LOT_THRESHOLD)
            .ageDecayLambda(AGE_DECAY_LAMBDA)
            .minEtaAge(MIN_ETA_AGE)
            .maintDecayLambda(MAINT_DECAY_LAMBDA)
            .ageFactor(AGE_FACTOR)
            .maintFactor(MAINT_FACTOR)
            .envFactor(ENV_FACTOR)
            .materialFactor(MATERIAL_FACTOR)
            .eIdxMax(EIDX_MAX)
            .baselineAgeFactor(BASELINE_AGE_FACTOR)
            .shieldingRelief(SHIELDING_RELIEF)
            .challengeBonusScale(CHALLENGE_BONUS_SCALE)
            .challengeBonusCap(CHALLENGE_BONUS_CAP)
            .gradeBoundaryA(GRADE_BOUNDARY_A)
            .gradeBoundaryB(GRADE_BOUNDARY_B)
            .build();
    }

    public QuantitativeScoringPolicy resolvePolicy(String policyConfig) {
        if (policyConfig == null || policyConfig.isBlank()) {
            return defaultPolicy;
        }

        try {
            JsonNode root = objectMapper.readTree(policyConfig);
            return QuantitativeScoringPolicy.builder()
                .uphWeight(number(root, defaultPolicy.getUphWeight(), "score.uphWeight", "uphWeight"))
                .yieldWeight(number(root, defaultPolicy.getYieldWeight(), "score.yieldWeight", "yieldWeight"))
                .leadTimeWeight(number(root, defaultPolicy.getLeadTimeWeight(), "score.leadTimeWeight", "leadTimeWeight"))
                .defaultTempWeight(number(root, defaultPolicy.getDefaultTempWeight(), "environment.tempWeight", "defaultTempWeight"))
                .defaultHumidityWeight(number(root, defaultPolicy.getDefaultHumidityWeight(), "environment.humidityWeight", "defaultHumidityWeight"))
                .defaultParticleWeight(number(root, defaultPolicy.getDefaultParticleWeight(), "environment.particleWeight", "defaultParticleWeight"))
                .defaultLotThreshold(number(root, defaultPolicy.getDefaultLotThreshold(), "material.lotDefectThreshold", "defaultLotThreshold"))
                .ageDecayLambda(number(root, defaultPolicy.getAgeDecayLambda(), "equipment.ageDecayLambda", "ageDecayLambda"))
                .minEtaAge(number(root, defaultPolicy.getMinEtaAge(), "equipment.minEtaAge", "minEtaAge"))
                .maintDecayLambda(number(root, defaultPolicy.getMaintDecayLambda(), "equipment.maintDecayLambda", "maintDecayLambda"))
                .ageFactor(number(root, defaultPolicy.getAgeFactor(), "equipment.ageFactor", "ageFactor"))
                .maintFactor(number(root, defaultPolicy.getMaintFactor(), "equipment.maintFactor", "maintFactor"))
                .envFactor(number(root, defaultPolicy.getEnvFactor(), "equipment.envFactor", "envFactor"))
                .materialFactor(number(root, defaultPolicy.getMaterialFactor(), "equipment.materialFactor", "materialFactor"))
                .eIdxMax(number(root, defaultPolicy.getEIdxMax(), "equipment.eIdxMax", "eIdxMax"))
                .baselineAgeFactor(number(root, defaultPolicy.getBaselineAgeFactor(), "equipment.baselineAgeFactor", "baselineAgeFactor"))
                .shieldingRelief(number(root, defaultPolicy.getShieldingRelief(), "material.shieldingRelief", "shieldingRelief"))
                .challengeBonusScale(number(root, defaultPolicy.getChallengeBonusScale(), "challenge.bonusScale", "challengeBonusScale"))
                .challengeBonusCap(number(root, defaultPolicy.getChallengeBonusCap(), "challenge.bonusCap", "challengeBonusCap"))
                .gradeBoundaryA(number(root, defaultPolicy.getGradeBoundaryA(), "equipment.gradeBoundaryA", "gradeBoundaryA"))
                .gradeBoundaryB(number(root, defaultPolicy.getGradeBoundaryB(), "equipment.gradeBoundaryB", "gradeBoundaryB"))
                .build();
        } catch (Exception ignored) {
            return defaultPolicy;
        }
    }

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

    public Integer calculateEquipmentAgeMonths(
        LocalDate equipmentInstallDate,
        LocalDate evaluationPeriodEndDate
    ) {
        if (equipmentInstallDate == null || evaluationPeriodEndDate == null) {
            return null;
        }

        long ageMonths = Math.max(0, ChronoUnit.MONTHS.between(
            YearMonth.from(equipmentInstallDate),
            YearMonth.from(evaluationPeriodEndDate)
        ));
        return Math.toIntExact(Math.min(ageMonths, Integer.MAX_VALUE));
    }

    public BigDecimal calculateEtaAge(BigDecimal equipmentWearCoefficient, BigDecimal nAge) {
        return calculateEtaAge(equipmentWearCoefficient, nAge, defaultPolicy);
    }

    public BigDecimal calculateEtaAge(
        BigDecimal equipmentWearCoefficient,
        BigDecimal nAge,
        QuantitativeScoringPolicy policy
    ) {
        if (equipmentWearCoefficient == null || nAge == null) {
            return scale(ONE);
        }

        double wearCoefficient = Math.max(BigDecimal.ZERO.doubleValue(), equipmentWearCoefficient.doubleValue());
        if (wearCoefficient <= 0) {
            return scale(ONE);
        }

        double normalizedAge = safeRatio(nAge).max(BigDecimal.ZERO).min(ONE).doubleValue();
        double decayBase = positiveOrDefault(policy.getAgeDecayLambda(), AGE_DECAY_LAMBDA).doubleValue() * wearCoefficient;
        double degradation = (Math.exp(decayBase * normalizedAge) - 1) / (Math.exp(decayBase) - 1);
        BigDecimal minEtaAge = clampRatio(positiveOrDefault(policy.getMinEtaAge(), MIN_ETA_AGE));
        double etaAge = minEtaAge.doubleValue() + (ONE.subtract(minEtaAge).doubleValue() * (1 - degradation));
        return clampRatio(BigDecimal.valueOf(etaAge));
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
        return calculateEtaMaint(maintenanceScoreNorm, defaultPolicy);
    }

    public BigDecimal calculateEtaMaint(BigDecimal maintenanceScoreNorm, QuantitativeScoringPolicy policy) {
        if (maintenanceScoreNorm == null) {
            return scale(ONE);
        }
        BigDecimal normalized = clampRatio(maintenanceScoreNorm);
        double exponent = -positiveOrDefault(policy.getMaintDecayLambda(), MAINT_DECAY_LAMBDA).doubleValue()
            * ONE.subtract(normalized).doubleValue();
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

        return calculateNEnv(
            environmentTemperature,
            environmentTempMin,
            environmentTempMax,
            environmentHumidity,
            environmentHumidityMin,
            environmentHumidityMax,
            environmentParticleCount,
            environmentParticleLimit,
            environmentTempWeight,
            environmentHumidityWeight,
            environmentParticleWeight,
            defaultPolicy
        );
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
        BigDecimal environmentParticleWeight,
        QuantitativeScoringPolicy policy
    ) {
        BigDecimal vTemp = calculateRangeDeviation(environmentTemperature, environmentTempMin, environmentTempMax);
        BigDecimal vHumidity = calculateRangeDeviation(environmentHumidity, environmentHumidityMin, environmentHumidityMax);
        BigDecimal vParticle = calculateParticleDeviation(environmentParticleCount, environmentParticleLimit);

        BigDecimal tempWeight = positiveOrDefault(environmentTempWeight, positiveOrDefault(policy.getDefaultTempWeight(), DEFAULT_TEMP_WEIGHT));
        BigDecimal humidityWeight = positiveOrDefault(environmentHumidityWeight, positiveOrDefault(policy.getDefaultHumidityWeight(), DEFAULT_HUMIDITY_WEIGHT));
        BigDecimal particleWeight = positiveOrDefault(environmentParticleWeight, positiveOrDefault(policy.getDefaultParticleWeight(), DEFAULT_PARTICLE_WEIGHT));
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
        return calculateMaterialShielding(defectiveWorkersSameLot, totalWorkersSameLot, lotDefectThreshold, defaultPolicy);
    }

    public BigDecimal calculateMaterialShielding(
        Integer defectiveWorkersSameLot,
        Integer totalWorkersSameLot,
        BigDecimal lotDefectThreshold,
        QuantitativeScoringPolicy policy
    ) {
        if (defectiveWorkersSameLot == null || totalWorkersSameLot == null || totalWorkersSameLot <= 0) {
            return ZERO;
        }
        BigDecimal threshold = positiveOrDefault(lotDefectThreshold, positiveOrDefault(policy.getDefaultLotThreshold(), DEFAULT_LOT_THRESHOLD));
        BigDecimal spikeRatio = BigDecimal.valueOf(defectiveWorkersSameLot)
            .divide(BigDecimal.valueOf(Math.max(totalWorkersSameLot, 1)), 4, RoundingMode.HALF_UP);
        return spikeRatio.compareTo(threshold) >= 0 ? scale(ONE) : ZERO;
    }

    public BigDecimal resolveMaterialShielding(
        Integer defectiveWorkersSameLot,
        Integer totalWorkersSameLot,
        BigDecimal lotDefectThreshold,
        BatchPeriodType periodType
    ) {
        return resolveMaterialShielding(defectiveWorkersSameLot, totalWorkersSameLot, lotDefectThreshold, periodType, defaultPolicy);
    }

    public BigDecimal resolveMaterialShielding(
        Integer defectiveWorkersSameLot,
        Integer totalWorkersSameLot,
        BigDecimal lotDefectThreshold,
        BatchPeriodType periodType,
        QuantitativeScoringPolicy policy
    ) {
        if (periodType != BatchPeriodType.MONTH) {
            return ZERO;
        }
        return calculateMaterialShielding(defectiveWorkersSameLot, totalWorkersSameLot, lotDefectThreshold, policy);
    }

    public BigDecimal calculateEIdx(
        String equipmentGrade,
        BigDecimal nAge,
        BigDecimal etaAge,
        BigDecimal etaMaint,
        BigDecimal nEnv,
        BigDecimal materialShielding
    ) {
        return calculateEIdx(equipmentGrade, nAge, etaAge, etaMaint, nEnv, materialShielding, defaultPolicy);
    }

    public BigDecimal calculateEIdx(
        String equipmentGrade,
        BigDecimal nAge,
        BigDecimal etaAge,
        BigDecimal etaMaint,
        BigDecimal nEnv,
        BigDecimal materialShielding,
        QuantitativeScoringPolicy policy
    ) {
        if (isProtectedGrade(equipmentGrade) || safeRatio(nAge).compareTo(BigDecimal.ZERO) <= 0) {
            return scale(ONE);
        }

        BigDecimal eIdx = ONE
            .add(positiveOrDefault(policy.getAgeFactor(), AGE_FACTOR).multiply(ONE.subtract(safeRatio(etaAge))))
            .add(positiveOrDefault(policy.getMaintFactor(), MAINT_FACTOR).multiply(ONE.subtract(safeRatio(etaMaint))))
            .add(positiveOrDefault(policy.getEnvFactor(), ENV_FACTOR).multiply(safeRatio(nEnv)))
            .add(positiveOrDefault(policy.getMaterialFactor(), MATERIAL_FACTOR).multiply(safeRatio(materialShielding)));

        return scale(eIdx.max(ONE).min(positiveOrDefault(policy.getEIdxMax(), EIDX_MAX)));
    }

    public String resolveCurrentEquipmentGrade(String initialEquipmentGrade, BigDecimal nAge) {
        return resolveCurrentEquipmentGrade(initialEquipmentGrade, nAge, defaultPolicy);
    }

    public String resolveCurrentEquipmentGrade(
        String initialEquipmentGrade,
        BigDecimal nAge,
        QuantitativeScoringPolicy policy
    ) {
        int initialRank = resolveEquipmentGradeRank(initialEquipmentGrade);
        int ageRank = resolveAgeDerivedEquipmentGradeRank(nAge, policy);
        int currentRank = Math.max(initialRank, ageRank);

        return switch (currentRank) {
            case 1 -> "S";
            case 2 -> "A";
            case 3 -> "B";
            default -> "C";
        };
    }

    public BigDecimal calculateEquipmentGradeIdx(String equipmentGrade) {
        if (equipmentGrade == null || equipmentGrade.isBlank()) {
            return null;
        }

        return switch (equipmentGrade.trim().toUpperCase()) {
            case "S" -> scale(ONE);
            case "A" -> scale(BigDecimal.valueOf(0.90));
            case "B" -> scale(BigDecimal.valueOf(0.80));
            case "C" -> scale(BigDecimal.valueOf(0.70));
            default -> null;
        };
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
        return calculateBaselineError(baselineError, errorReferenceRate, nAge, defaultPolicy);
    }

    public BigDecimal calculateBaselineError(
        BigDecimal baselineError,
        BigDecimal errorReferenceRate,
        BigDecimal nAge,
        QuantitativeScoringPolicy policy
    ) {
        if (isPositive(baselineError)) {
            return scale(baselineError);
        }
        if (!isPositive(errorReferenceRate)) {
            return ZERO;
        }
        BigDecimal calculated = errorReferenceRate.multiply(
            ONE.add(positiveOrDefault(policy.getBaselineAgeFactor(), BASELINE_AGE_FACTOR).multiply(safeRatio(nAge)))
        );
        return scale(calculated);
    }

    public BigDecimal calculateAdjustedBaselineError(BigDecimal baselineError, BigDecimal eIdx) {
        if (!isPositive(baselineError)) {
            return ZERO;
        }
        return scale(baselineError.multiply(positiveOrDefault(eIdx, ONE)));
    }

    public BigDecimal calculateQBase(BigDecimal uphScore, BigDecimal yieldScore, BigDecimal leadTimeScore) {
        return calculateQBase(uphScore, yieldScore, leadTimeScore, defaultPolicy);
    }

    public BigDecimal calculateQBase(
        BigDecimal uphScore,
        BigDecimal yieldScore,
        BigDecimal leadTimeScore,
        QuantitativeScoringPolicy policy
    ) {
        BigDecimal qBase = safe(uphScore).multiply(positiveOrDefault(policy.getUphWeight(), UPH_WEIGHT))
            .add(safe(yieldScore).multiply(positiveOrDefault(policy.getYieldWeight(), YIELD_WEIGHT)))
            .add(safe(leadTimeScore).multiply(positiveOrDefault(policy.getLeadTimeWeight(), LEAD_TIME_WEIGHT)));
        return clampPercentage(qBase);
    }

    public BigDecimal calculateEffectiveActualError(BigDecimal actualError, BigDecimal materialShielding) {
        return calculateEffectiveActualError(actualError, materialShielding, defaultPolicy);
    }

    public BigDecimal calculateEffectiveActualError(
        BigDecimal actualError,
        BigDecimal materialShielding,
        QuantitativeScoringPolicy policy
    ) {
        if (!isPositive(actualError)) {
            return ZERO;
        }
        BigDecimal effectiveError = actualError.multiply(
            ONE.subtract(positiveOrDefault(policy.getShieldingRelief(), SHIELDING_RELIEF).multiply(safeRatio(materialShielding)))
        );
        return scale(effectiveError.max(BigDecimal.ZERO));
    }

    public BigDecimal calculateBonusPoint(BigDecimal difficultyScore, String difficultyGrade, String currentSkillTier) {
        return calculateBonusPoint(difficultyScore, difficultyGrade, currentSkillTier, defaultPolicy);
    }

    public BigDecimal calculateBonusPoint(
        BigDecimal difficultyScore,
        String difficultyGrade,
        String currentSkillTier,
        QuantitativeScoringPolicy policy
    ) {
        BigDecimal difficultyCapability = resolveDifficultyCapability(difficultyScore, difficultyGrade);
        BigDecimal workerCapability = resolveWorkerCapability(currentSkillTier);

        if (!isPositive(difficultyCapability) || !isPositive(workerCapability)) {
            return ZERO;
        }

        BigDecimal capabilityGap = difficultyCapability.subtract(workerCapability);
        if (capabilityGap.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }

        return scale(capabilityGap
            .multiply(positiveOrDefault(policy.getChallengeBonusScale(), CHALLENGE_BONUS_SCALE))
            .min(positiveOrDefault(policy.getChallengeBonusCap(), CHALLENGE_BONUS_CAP)));
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
        return periodType == BatchPeriodType.MONTH ? "CONFIRMED" : "TEMPORARY";
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

    private int resolveEquipmentGradeRank(String equipmentGrade) {
        if (equipmentGrade == null || equipmentGrade.isBlank()) {
            return 4;
        }

        return switch (equipmentGrade.trim().toUpperCase()) {
            case "S" -> 1;
            case "A" -> 2;
            case "B" -> 3;
            default -> 4;
        };
    }

    private int resolveAgeDerivedEquipmentGradeRank(BigDecimal nAge) {
        return resolveAgeDerivedEquipmentGradeRank(nAge, defaultPolicy);
    }

    private int resolveAgeDerivedEquipmentGradeRank(BigDecimal nAge, QuantitativeScoringPolicy policy) {
        BigDecimal normalizedAge = safeRatio(nAge);
        if (normalizedAge.compareTo(BigDecimal.ZERO) <= 0) {
            return 1;
        }
        if (normalizedAge.compareTo(positiveOrDefault(policy.getGradeBoundaryA(), GRADE_BOUNDARY_A)) <= 0) {
            return 2;
        }
        if (normalizedAge.compareTo(positiveOrDefault(policy.getGradeBoundaryB(), GRADE_BOUNDARY_B)) <= 0) {
            return 3;
        }
        return 4;
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

    private BigDecimal number(JsonNode root, BigDecimal defaultValue, String... paths) {
        for (String path : paths) {
            JsonNode node = find(root, path);
            if (node == null || node.isMissingNode() || node.isNull()) {
                continue;
            }
            if (node.isNumber()) {
                return node.decimalValue();
            }
            if (node.isTextual() && !node.asText().isBlank()) {
                try {
                    return new BigDecimal(node.asText().trim());
                } catch (NumberFormatException ignored) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    private JsonNode find(JsonNode root, String dottedPath) {
        JsonNode current = root;
        for (String segment : dottedPath.split("\\.")) {
            if (current == null) {
                return null;
            }
            current = current.get(segment);
        }
        return current;
    }
}
