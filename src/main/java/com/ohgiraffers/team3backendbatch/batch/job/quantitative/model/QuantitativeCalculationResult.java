package com.ohgiraffers.team3backendbatch.batch.job.quantitative.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QuantitativeCalculationResult {

    private final BigDecimal actualError;
    private final BigDecimal nAge;
    private final BigDecimal etaAge;
    private final BigDecimal nMaint;
    private final BigDecimal etaMaint;
    private final BigDecimal nEnv;
    private final BigDecimal materialShielding;
    private final BigDecimal uphScore;
    private final BigDecimal yieldScore;
    private final BigDecimal leadTimeScore;
    private final BigDecimal difficultyAdjustment;
    private final BigDecimal baselineError;
    private final BigDecimal qBase;
    private final BigDecimal eIdx;
    private final BigDecimal bonusPoint;
    private final BigDecimal provisionalSQuant;
    private final BigDecimal environmentCorrection;
    private final BigDecimal materialCorrection;
    private final BigDecimal antiGamingPenalty;
    private final BigDecimal sQuant;
    private final BigDecimal tScore;
    private final String status;
}
