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

    /**
     * 정량 평가 원천 데이터를 한 건씩 반환한다.
     * @param 없음
     * @return 정량 평가 집계 데이터
     */
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

    /**
     * 정량 평가 대상 평가 기간 ID 를 결정한다.
     * @param 없음
     * @return 평가 기간 ID
     */
    private Long resolveEvaluationPeriodId() {
        if (evaluationPeriodId != null) {
            return evaluationPeriodId;
        }

        return evaluationPeriodQueryMapper.findLatestConfirmedPeriod(periodType)
            .map(com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodProjectionRow::getEvaluationPeriodId)
            .orElse(null);
    }

    /**
     * 월간 평가 대상의 그룹 통계를 보강한다.
     * @param rawItems 정량 평가 집계 원본 목록
     * @return 그룹 통계가 반영된 정량 평가 집계 목록
     */
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

    /**
     * 그룹 통계 계산용 미리보기 집계 결과를 생성한다.
     * @param item 정량 평가 집계 데이터
     * @return 계산 완료된 정량 평가 집계 데이터
     */
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

    /**
     * 동일 그룹의 평균과 표준편차를 계산한다.
     * @param items 동일 그룹 정량 평가 집계 목록
     * @return 그룹 통계 값
     */
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

    /**
     * 계산된 그룹 통계를 집계 데이터에 반영한다.
     * @param item 정량 평가 집계 데이터
     * @param groupStats 반영할 그룹 통계
     * @return 그룹 통계가 반영된 정량 평가 집계 데이터
     */
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

    /**
     * 그룹 통계 계산용 그룹 키를 생성한다.
     * @param item 정량 평가 집계 데이터
     * @return 그룹 키
     */
    private String resolveGroupKey(QuantitativeEvaluationAggregate item) {
        return item.getCurrentSkillTier() == null || item.getCurrentSkillTier().isBlank()
            ? "UNKNOWN"
            : item.getCurrentSkillTier().trim().toUpperCase();
    }

    /**
     * 조회 row 를 정량 평가 집계 객체로 변환한다.
     * @param row 정량 평가 원천 조회 row
     * @return 정량 평가 집계 객체
     */
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
            .unresolvedEnvironmentEventCount(row.getUnresolvedEnvironmentEventCount())
            .failedLotCount(row.getFailedLotCount())
            .totalLotCount(row.getTotalLotCount())
            .peerLotFailRate(row.getPeerLotFailRate())
            .lotDefectThreshold(row.getLotDefectThreshold())
            .difficultyScore(row.getDifficultyScore())
            .difficultyGrade(row.getDifficultyGrade())
            .currentSkillTier(row.getCurrentSkillTier())
            .errorReferenceRate(row.getErrorReferenceRate())
            .baselineError(row.getBaselineError())
            .antiGamingPenalty(row.getAntiGamingPenalty())
            .groupMean(row.getGroupMean())
            .groupStdDev(row.getGroupStdDev())
            .actualError(row.getActualError())
            .build();
    }

    /**
     * 문자열 periodType 값을 배치 enum 으로 변환한다.
     * @param value 요청된 periodType 문자열
     * @return 배치 평가 기간 유형
     */
    private BatchPeriodType parsePeriodType(String value) {
        if (value == null || value.isBlank()) {
            return BatchPeriodType.MONTH;
        }
        return BatchPeriodType.valueOf(value.trim().toUpperCase());
    }

    /**
     * 소수점 둘째 자리로 값을 보정한다.
     * @param value 보정할 수치 값
     * @return 보정된 수치 값
     */
    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private record GroupStats(BigDecimal mean, BigDecimal stdDev) {
    }
}
