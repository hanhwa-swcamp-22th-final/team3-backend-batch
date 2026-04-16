package com.ohgiraffers.team3backendbatch.domain.qualitative.scoring;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.ChunkContribution;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Calculates raw and normalized qualitative score values.
 */
@Component
public class QualitativeScoreCalculator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ONE_POINT_FIVE = BigDecimal.valueOf(1.5);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal FIFTY = BigDecimal.valueOf(50);
    private static final BigDecimal TEN = BigDecimal.TEN;
    private static final BigDecimal DEFAULT_GROUP_MEAN = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_GROUP_STD = BigDecimal.valueOf(0.5);
    private static final BigDecimal EPSILON = BigDecimal.valueOf(0.01);
    private static final BigDecimal SECONDARY_ADJUSTMENT_FACTOR = BigDecimal.valueOf(0.5);
    private static final BigDecimal SECONDARY_ADJUSTMENT_CAP = BigDecimal.valueOf(0.5);
    private static final BigDecimal INTERNAL_RAW_MIN = BigDecimal.valueOf(-1.76);
    private static final BigDecimal INTERNAL_RAW_MAX = BigDecimal.valueOf(1.76);
    private static final BigDecimal INTERNAL_RAW_SPAN = INTERNAL_RAW_MAX.subtract(INTERNAL_RAW_MIN);

    /**
     * Calculates the raw score of a chunk from sentiment, keyword weights, and negation.
     * @param sentimentScore sentiment score returned from NLP analysis
     * @param keywordWeightSum summed keyword weight for the chunk
     * @param negationDetected whether negation was detected in the chunk
     * @return calculated raw chunk score
     */
    public BigDecimal calculateChunkScore(
        BigDecimal sentimentScore,
        BigDecimal keywordWeightSum,
        boolean negationDetected
    ) {
        BigDecimal base = sentimentScore.add(keywordWeightSum);
        if (negationDetected) {
            base = base.negate();
        }
        return base.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the weighted average of chunk scores with contrastive emphasis.
     * @param contributions chunk contributions collected from the comment
     * @return weighted average raw score
     */
    public BigDecimal calculateWeightedAverage(List<ChunkContribution> contributions) {
        if (contributions == null || contributions.isEmpty()) {
            return ZERO.setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal weightedSum = ZERO;
        BigDecimal totalWeight = ZERO;

        for (ChunkContribution contribution : contributions) {
            BigDecimal weight = contribution.isContrastive() ? ONE_POINT_FIVE : ONE;
            weightedSum = weightedSum.add(contribution.getChunkScore().multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        if (ZERO.compareTo(totalWeight) == 0) {
            return ZERO.setScale(4, RoundingMode.HALF_UP);
        }

        return weightedSum.divide(totalWeight, 4, RoundingMode.HALF_UP);
    }

    /**
     * Applies the context weight to the aggregated comment raw score.
     * @param rawScore aggregated raw score before context weighting
     * @param contextWeight context weight derived from matched rules
     * @return weighted raw score
     */
    public BigDecimal applyContextWeight(BigDecimal rawScore, BigDecimal contextWeight) {
        BigDecimal effectiveWeight = contextWeight == null ? ONE : contextWeight;
        return rawScore.multiply(effectiveWeight).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Converts the internal raw score into the display score range of 0 to 100.
     * @param internalRawScore internal raw score
     * @return display score scaled to 0 through 100
     */
    public BigDecimal scaleInternalRawToDisplayScore(BigDecimal internalRawScore) {
        BigDecimal clampedRaw = clampInternalRawScore(internalRawScore);
        return clampedRaw.subtract(INTERNAL_RAW_MIN)
            .multiply(HUNDRED)
            .divide(INTERNAL_RAW_SPAN, 2, RoundingMode.HALF_UP);
    }

    /**
     * Converts the internal adjustment value into a display delta.
     * @param internalAdjustmentScore internal adjustment score
     * @return display delta value
     */
    public BigDecimal scaleInternalAdjustmentToDisplayDelta(BigDecimal internalAdjustmentScore) {
        if (internalAdjustmentScore == null) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return internalAdjustmentScore.multiply(HUNDRED)
            .divide(INTERNAL_RAW_SPAN, 2, RoundingMode.HALF_UP);
    }

    /**
     * Normalizes a raw score into the default S-QUAL score scale.
     * @param rawScore raw qualitative score
     * @return normalized S-QUAL score
     */
    public BigDecimal normalizeToSQual(BigDecimal rawScore) {
        return normalizeToTScore(rawScore, DEFAULT_GROUP_MEAN, DEFAULT_GROUP_STD);
    }

    /**
     * Normalizes a raw score into a T-score using the provided group statistics.
     * @param rawScore raw qualitative score
     * @param groupMean group mean used for normalization
     * @param groupStd group standard deviation used for normalization
     * @return normalized T-score
     */
    public BigDecimal normalizeToTScore(BigDecimal rawScore, BigDecimal groupMean, BigDecimal groupStd) {
        BigDecimal mean = groupMean == null ? DEFAULT_GROUP_MEAN : groupMean;
        BigDecimal std = groupStd == null ? DEFAULT_GROUP_STD : groupStd;
        if (std.compareTo(EPSILON) < 0) {
            std = EPSILON;
        }

        BigDecimal scaled = FIFTY.add(
            TEN.multiply(rawScore.subtract(mean).divide(std, 6, RoundingMode.HALF_UP))
        );

        if (scaled.compareTo(ZERO) < 0) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (scaled.compareTo(HUNDRED) > 0) {
            return HUNDRED.setScale(2, RoundingMode.HALF_UP);
        }
        return scaled.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the secondary adjustment raw value from the second evaluator comment score.
     * @param commentRawScore second evaluator raw score
     * @return capped secondary adjustment raw value
     */
    public BigDecimal calculateSecondaryAdjustmentRaw(BigDecimal commentRawScore) {
        BigDecimal adjustment = commentRawScore.multiply(SECONDARY_ADJUSTMENT_FACTOR)
            .setScale(4, RoundingMode.HALF_UP);

        if (adjustment.compareTo(SECONDARY_ADJUSTMENT_CAP.negate()) < 0) {
            return SECONDARY_ADJUSTMENT_CAP.negate().setScale(4, RoundingMode.HALF_UP);
        }
        if (adjustment.compareTo(SECONDARY_ADJUSTMENT_CAP) > 0) {
            return SECONDARY_ADJUSTMENT_CAP.setScale(4, RoundingMode.HALF_UP);
        }
        return adjustment;
    }

    /**
     * Applies an adjustment value to a raw score.
     * @param baseRawScore base raw score
     * @param adjustmentScore adjustment value
     * @return adjusted raw score
     */
    public BigDecimal applyRawAdjustment(BigDecimal baseRawScore, BigDecimal adjustmentScore) {
        return baseRawScore.add(adjustmentScore).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Applies an adjustment to a display score and clamps the result to 0 through 100.
     * @param baseRawScore base display score
     * @param adjustmentScore display adjustment value
     * @return adjusted display score
     */
    public BigDecimal applyDisplayRawAdjustment(BigDecimal baseRawScore, BigDecimal adjustmentScore) {
        BigDecimal adjusted = baseRawScore.add(adjustmentScore);
        if (adjusted.compareTo(ZERO) < 0) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (adjusted.compareTo(HUNDRED) > 0) {
            return HUNDRED.setScale(2, RoundingMode.HALF_UP);
        }
        return adjusted.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Classifies the score into an S, A, B, or C tier.
     * @param sQual normalized qualitative score
     * @return tier code derived from the score
     */
    public String classifyTier(BigDecimal sQual) {
        if (sQual.compareTo(BigDecimal.valueOf(85)) >= 0) {
            return "S";
        }
        if (sQual.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "A";
        }
        if (sQual.compareTo(BigDecimal.valueOf(55)) >= 0) {
            return "B";
        }
        return "C";
    }

    private BigDecimal clampInternalRawScore(BigDecimal internalRawScore) {
        if (internalRawScore == null) {
            return ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        if (internalRawScore.compareTo(INTERNAL_RAW_MIN) < 0) {
            return INTERNAL_RAW_MIN.setScale(4, RoundingMode.HALF_UP);
        }
        if (internalRawScore.compareTo(INTERNAL_RAW_MAX) > 0) {
            return INTERNAL_RAW_MAX.setScale(4, RoundingMode.HALF_UP);
        }
        return internalRawScore.setScale(4, RoundingMode.HALF_UP);
    }
}
