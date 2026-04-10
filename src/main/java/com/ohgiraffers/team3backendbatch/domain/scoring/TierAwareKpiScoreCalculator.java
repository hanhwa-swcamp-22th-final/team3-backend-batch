package com.ohgiraffers.team3backendbatch.domain.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TierAwareKpiScoreCalculator {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final Set<String> STRATEGIC_TIERS = Set.of("A", "S");

    public boolean isStrategicTier(String employeeTier) {
        return employeeTier != null && STRATEGIC_TIERS.contains(employeeTier.trim().toUpperCase());
    }

    public BigDecimal resolveQuantitativeSettlementScore(
        String employeeTier,
        BigDecimal fallbackTScore,
        BigDecimal productivityScore,
        BigDecimal qualityScore,
        BigDecimal equipmentResponseScore
    ) {
        if (!isStrategicTier(employeeTier)) {
            return sanitize(fallbackTScore);
        }

        BigDecimal blended = blend(
            new BigDecimal[] { productivityScore, qualityScore, equipmentResponseScore },
            new BigDecimal[] { new BigDecimal("0.20"), new BigDecimal("0.35"), new BigDecimal("0.45") }
        );
        return blended != null ? blended : sanitize(fallbackTScore);
    }

    public BigDecimal toKmsSignalScore(int approvedArticleCount) {
        if (approvedArticleCount <= 0) {
            return null;
        }
        return sanitize(BigDecimal.valueOf(Math.min(approvedArticleCount * 25L, 100L)));
    }

    public BigDecimal toChallengeSignalScore(int challengeCount) {
        if (challengeCount <= 0) {
            return null;
        }
        return sanitize(BigDecimal.valueOf(Math.min(challengeCount * 20L, 100L)));
    }

    private BigDecimal blend(BigDecimal[] scores, BigDecimal[] weights) {
        BigDecimal totalWeight = ZERO;
        BigDecimal weightedSum = ZERO;

        for (int index = 0; index < scores.length; index++) {
            BigDecimal score = scores[index];
            BigDecimal weight = weights[index];
            if (score == null || weight == null) {
                continue;
            }
            weightedSum = weightedSum.add(sanitize(score).multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return sanitize(weightedSum.divide(totalWeight, 4, RoundingMode.HALF_UP));
    }

    private BigDecimal sanitize(BigDecimal score) {
        if (score == null) {
            return null;
        }
        return score.max(BigDecimal.ZERO).min(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }
}
