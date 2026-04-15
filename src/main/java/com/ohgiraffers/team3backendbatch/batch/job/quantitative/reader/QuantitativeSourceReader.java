package com.ohgiraffers.team3backendbatch.batch.job.quantitative.reader;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationSourceRow;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.processor.QuantitativeEvaluationProcessor;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.QuantitativeEvaluationQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class QuantitativeSourceReader implements ItemReader<QuantitativeEvaluationAggregate> {

    private static final Logger log = LoggerFactory.getLogger(QuantitativeSourceReader.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final QuantitativeEvaluationQueryMapper quantitativeEvaluationQueryMapper;
    private final QuantitativeEvaluationProcessor quantitativeEvaluationProcessor;
    private final EvaluationPeriodQueryMapper evaluationPeriodQueryMapper;
    private final Long evaluationPeriodId;
    private final Long employeeId;
    private final boolean force;
    private final BatchPeriodType periodType;

    private Iterator<QuantitativeEvaluationAggregate> iterator;

    public QuantitativeSourceReader(
        QuantitativeEvaluationQueryMapper quantitativeEvaluationQueryMapper,
        QuantitativeEvaluationProcessor quantitativeEvaluationProcessor,
        EvaluationPeriodQueryMapper evaluationPeriodQueryMapper,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId,
        @Value("#{jobParameters['employeeId']}") Long employeeId,
        @Value("#{jobParameters['force']}") String force,
        @Value("#{jobParameters['periodType']}") String periodType
    ) {
        this.quantitativeEvaluationQueryMapper = quantitativeEvaluationQueryMapper;
        this.quantitativeEvaluationProcessor = quantitativeEvaluationProcessor;
        this.evaluationPeriodQueryMapper = evaluationPeriodQueryMapper;
        this.evaluationPeriodId = evaluationPeriodId;
        this.employeeId = employeeId;
        this.force = Boolean.parseBoolean(force);
        this.periodType = parsePeriodType(periodType);
    }

    @Override
    public QuantitativeEvaluationAggregate read() {
        if (iterator == null) {
            Long resolvedEvaluationPeriodId = resolveEvaluationPeriodId();
            if (resolvedEvaluationPeriodId == null) {
                log.warn(
                    "No evaluation period found for quantitative evaluation job. requestedEvaluationPeriodId={}, periodType={}",
                    evaluationPeriodId,
                    periodType
                );
                return null;
            }

            List<QuantitativeEvaluationAggregate> items = enrichMonthlyGroupStatistics(quantitativeEvaluationQueryMapper
                .findQuantitativeSourcesForEvaluation(resolvedEvaluationPeriodId, employeeId, force)
                .stream()
                .map(this::toAggregate)
                .toList());

            iterator = items.iterator();
            log.info(
                "Loaded quantitative source rows. evaluationPeriodId={}, employeeId={}, periodType={}, force={}, count={}",
                resolvedEvaluationPeriodId,
                employeeId,
                periodType,
                force,
                items.size()
            );
        }

        if (!iterator.hasNext()) {
            return null;
        }
        return iterator.next();
    }

    private Long resolveEvaluationPeriodId() {
        if (evaluationPeriodId != null) {
            return evaluationPeriodId;
        }

        return evaluationPeriodQueryMapper.findLatestConfirmedPeriod(periodType)
            .map(com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodProjectionRow::getEvaluationPeriodId)
            .orElse(null);
    }

    private List<QuantitativeEvaluationAggregate> enrichMonthlyGroupStatistics(List<QuantitativeEvaluationAggregate> rawItems) {
        if (periodType != BatchPeriodType.MONTH || rawItems.isEmpty()) {
            return rawItems;
        }

        List<QuantitativeEvaluationAggregate> previewItems = new ArrayList<>(rawItems.size());
        for (QuantitativeEvaluationAggregate item : rawItems) {
            previewItems.add(previewAggregate(item));
        }

        Map<String, GroupStats> statsByTier = previewItems.stream()
            .collect(Collectors.groupingBy(
                this::resolveGroupKey,
                Collectors.collectingAndThen(Collectors.toList(), this::calculateGroupStats)
            ));

        return previewItems.stream()
            .map(item -> applyGroupStats(item, statsByTier.get(resolveGroupKey(item))))
            .toList();
    }

    private QuantitativeEvaluationAggregate previewAggregate(QuantitativeEvaluationAggregate item) {
        try {
            return quantitativeEvaluationProcessor.process(item);
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Failed to prepare quantitative group statistics for employeeId=%s, equipmentId=%s"
                    .formatted(item.getEmployeeId(), item.getEquipmentId()),
                exception
            );
        }
    }

    private GroupStats calculateGroupStats(List<QuantitativeEvaluationAggregate> items) {
        List<BigDecimal> scores = items.stream()
            .map(QuantitativeEvaluationAggregate::getSQuant)
            .filter(Objects::nonNull)
            .toList();

        if (scores.isEmpty()) {
            return new GroupStats(null, null);
        }

        BigDecimal mean = scale(
            scores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP)
        );

        if (scores.size() < 2) {
            return new GroupStats(mean, ZERO);
        }

        BigDecimal variance = scores.stream()
            .map(score -> score.subtract(mean))
            .map(delta -> delta.multiply(delta))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP);

        BigDecimal stdDev = scale(BigDecimal.valueOf(Math.sqrt(Math.max(variance.doubleValue(), 0.0d))));
        return new GroupStats(mean, stdDev);
    }

    private QuantitativeEvaluationAggregate applyGroupStats(
        QuantitativeEvaluationAggregate item,
        GroupStats groupStats
    ) {
        if (groupStats == null) {
            return item;
        }
        return item.toBuilder()
            .groupMean(groupStats.mean())
            .groupStdDev(groupStats.stdDev())
            .build();
    }

    private String resolveGroupKey(QuantitativeEvaluationAggregate item) {
        return item.getCurrentSkillTier() == null || item.getCurrentSkillTier().isBlank()
            ? "UNKNOWN"
            : item.getCurrentSkillTier().trim().toUpperCase();
    }

    private QuantitativeEvaluationAggregate toAggregate(QuantitativeEvaluationSourceRow row) {
        return QuantitativeEvaluationAggregate.builder()
            .quantitativeEvaluationId(row.getQuantitativeEvaluationId())
            .employeeId(row.getEmployeeId())
            .evaluationPeriodId(row.getEvaluationPeriodId())
            .equipmentId(row.getEquipmentId())
            .periodType(periodType)
            .algorithmVersionId(row.getAlgorithmVersionId())
            .policyConfig(row.getPolicyConfig())
            .evaluationPeriodEndDate(row.getEvaluationPeriodEndDate())
            .totalInputQty(row.getTotalInputQty())
            .totalGoodQty(row.getTotalGoodQty())
            .totalDefectQty(row.getTotalDefectQty())
            .averageLeadTimeSec(row.getAverageLeadTimeSec())
            .downtimeMinutes(row.getDowntimeMinutes())
            .maintenanceMinutes(row.getMaintenanceMinutes())
            .targetUph(row.getTargetUph())
            .targetYieldRate(row.getTargetYieldRate())
            .targetLeadTimeSec(row.getTargetLeadTimeSec())
            .equipmentInstallDate(row.getEquipmentInstallDate())
            .equipmentGrade(row.getEquipmentGrade())
            .equipmentWarrantyMonths(row.getEquipmentWarrantyMonths())
            .equipmentDesignLifeMonths(row.getEquipmentDesignLifeMonths())
            .equipmentWearCoefficient(row.getEquipmentWearCoefficient())
            .maintenanceWeightedScoreSum(row.getMaintenanceWeightedScoreSum())
            .maintenanceWeightSum(row.getMaintenanceWeightSum())
            .environmentTemperature(row.getEnvironmentTemperature())
            .environmentHumidity(row.getEnvironmentHumidity())
            .environmentParticleCount(row.getEnvironmentParticleCount())
            .environmentTempMin(row.getEnvironmentTempMin())
            .environmentTempMax(row.getEnvironmentTempMax())
            .environmentHumidityMin(row.getEnvironmentHumidityMin())
            .environmentHumidityMax(row.getEnvironmentHumidityMax())
            .environmentParticleLimit(row.getEnvironmentParticleLimit())
            .environmentTempWeight(row.getEnvironmentTempWeight())
            .environmentHumidityWeight(row.getEnvironmentHumidityWeight())
            .environmentParticleWeight(row.getEnvironmentParticleWeight())
            .defectiveWorkersSameLot(row.getDefectiveWorkersSameLot())
            .totalWorkersSameLot(row.getTotalWorkersSameLot())
            .lotDefectThreshold(row.getLotDefectThreshold())
            .difficultyScore(row.getDifficultyScore())
            .difficultyGrade(row.getDifficultyGrade())
            .currentSkillTier(row.getCurrentSkillTier())
            .errorReferenceRate(row.getErrorReferenceRate())
            .baselineError(row.getBaselineError())
            .environmentCorrection(row.getEnvironmentCorrection())
            .materialCorrection(row.getMaterialCorrection())
            .antiGamingPenalty(row.getAntiGamingPenalty())
            .groupMean(row.getGroupMean())
            .groupStdDev(row.getGroupStdDev())
            .actualError(row.getActualError())
            .build();
    }

    private BatchPeriodType parsePeriodType(String value) {
        if (value == null || value.isBlank()) {
            return BatchPeriodType.MONTH;
        }
        return BatchPeriodType.valueOf(value.trim().toUpperCase());
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private record GroupStats(BigDecimal mean, BigDecimal stdDev) {
    }
}
