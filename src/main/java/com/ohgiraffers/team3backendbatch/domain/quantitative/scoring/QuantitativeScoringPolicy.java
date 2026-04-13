package com.ohgiraffers.team3backendbatch.domain.quantitative.scoring;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuantitativeScoringPolicy {

    private final BigDecimal uphWeight;
    private final BigDecimal yieldWeight;
    private final BigDecimal leadTimeWeight;
    private final BigDecimal defaultTempWeight;
    private final BigDecimal defaultHumidityWeight;
    private final BigDecimal defaultParticleWeight;
    private final BigDecimal defaultLotThreshold;
    private final BigDecimal ageDecayLambda;
    private final BigDecimal minEtaAge;
    private final BigDecimal maintDecayLambda;
    private final BigDecimal ageFactor;
    private final BigDecimal maintFactor;
    private final BigDecimal envFactor;
    private final BigDecimal materialFactor;
    private final BigDecimal eIdxMax;
    private final BigDecimal baselineAgeFactor;
    private final BigDecimal shieldingRelief;
    private final BigDecimal challengeBonusScale;
    private final BigDecimal challengeBonusCap;
    private final BigDecimal gradeBoundaryA;
    private final BigDecimal gradeBoundaryB;
}
