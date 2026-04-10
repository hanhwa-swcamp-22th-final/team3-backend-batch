package com.ohgiraffers.team3backendbatch.domain.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MonthlySkillContributionCalculator {

    public static final BigDecimal DEFAULT_ALPHA = new BigDecimal("0.01");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    public Map<String, BigDecimal> calculateMonthlySkillContributions(
        String employeeTier,
        BigDecimal quantitativeProductivityScore,
        BigDecimal quantitativeQualityScore,
        BigDecimal quantitativeEquipmentResponseScore,
        BigDecimal kmsSignalScore,
        BigDecimal challengeSignalScore,
        Map<String, BigDecimal> qualitativeSkillScores
    ) {
        Map<String, BigDecimal> contributions = new LinkedHashMap<>();
        Map<String, BigDecimal> qualitativeSignals = qualitativeSkillScores == null ? Map.of() : qualitativeSkillScores;
        boolean strategicTier = employeeTier != null
            && ("A".equalsIgnoreCase(employeeTier) || "S".equalsIgnoreCase(employeeTier));

        putIfPresent(contributions, "EQUIPMENT_RESPONSE",
            blend(
                qualitativeSignals.get("EQUIPMENT_RESPONSE"), strategicTier ? new BigDecimal("0.40") : new BigDecimal("0.50"),
                quantitativeEquipmentResponseScore, strategicTier ? new BigDecimal("0.35") : new BigDecimal("0.30"),
                kmsSignalScore, strategicTier ? new BigDecimal("0.25") : new BigDecimal("0.20")
            ));
        putIfPresent(contributions, "TECHNICAL_TRANSFER",
            blend(
                qualitativeSignals.get("TECHNICAL_TRANSFER"), strategicTier ? new BigDecimal("0.40") : new BigDecimal("0.60"),
                kmsSignalScore, strategicTier ? new BigDecimal("0.60") : new BigDecimal("0.40"),
                null, null
            ));
        putIfPresent(contributions, "INNOVATION_PROPOSAL",
            blend(
                qualitativeSignals.get("INNOVATION_PROPOSAL"), strategicTier ? new BigDecimal("0.35") : new BigDecimal("0.50"),
                challengeSignalScore, strategicTier ? new BigDecimal("0.40") : new BigDecimal("0.30"),
                kmsSignalScore, strategicTier ? new BigDecimal("0.25") : new BigDecimal("0.20")
            ));
        putIfPresent(contributions, "SAFETY_COMPLIANCE",
            blend(
                qualitativeSignals.get("SAFETY_COMPLIANCE"), new BigDecimal("0.40"),
                null, null,
                null, null
            ));
        putIfPresent(contributions, "QUALITY_MANAGEMENT",
            blend(
                qualitativeSignals.get("QUALITY_MANAGEMENT"), new BigDecimal("0.20"),
                quantitativeQualityScore, strategicTier ? new BigDecimal("0.55") : new BigDecimal("0.60"),
                kmsSignalScore, new BigDecimal("0.20")
            ));
        putIfPresent(contributions, "PRODUCTIVITY",
            blend(
                qualitativeSignals.get("PRODUCTIVITY"), strategicTier ? new BigDecimal("0.20") : new BigDecimal("0.30"),
                quantitativeProductivityScore, strategicTier ? new BigDecimal("0.50") : new BigDecimal("0.70"),
                null, null
            ));

        return contributions;
    }

    public BigDecimal getDefaultAlpha() {
        return DEFAULT_ALPHA;
    }

    private BigDecimal blend(
        BigDecimal qualitativeScore,
        BigDecimal qualitativeWeight,
        BigDecimal quantitativeScore,
        BigDecimal quantitativeWeight,
        BigDecimal auxiliaryScore,
        BigDecimal auxiliaryWeight
    ) {
        BigDecimal weightedSum = ZERO;
        BigDecimal totalWeight = ZERO;

        if (qualitativeScore != null && qualitativeWeight != null) {
            weightedSum = weightedSum.add(sanitize(qualitativeScore).multiply(qualitativeWeight));
            totalWeight = totalWeight.add(qualitativeWeight);
        }

        if (quantitativeScore != null && quantitativeWeight != null) {
            weightedSum = weightedSum.add(sanitize(quantitativeScore).multiply(quantitativeWeight));
            totalWeight = totalWeight.add(quantitativeWeight);
        }

        if (auxiliaryScore != null && auxiliaryWeight != null) {
            weightedSum = weightedSum.add(sanitize(auxiliaryScore).multiply(auxiliaryWeight));
            totalWeight = totalWeight.add(auxiliaryWeight);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return sanitize(weightedSum.divide(totalWeight, 4, RoundingMode.HALF_UP));
    }

    private void putIfPresent(Map<String, BigDecimal> contributions, String category, BigDecimal contribution) {
        if (contribution != null) {
            contributions.put(category, contribution);
        }
    }

    private BigDecimal sanitize(BigDecimal score) {
        if (score == null) {
            return ZERO;
        }
        return score.max(BigDecimal.ZERO).min(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }
}
