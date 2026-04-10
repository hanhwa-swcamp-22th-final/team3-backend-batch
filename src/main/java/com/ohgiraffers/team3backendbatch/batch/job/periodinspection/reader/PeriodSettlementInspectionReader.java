package com.ohgiraffers.team3backendbatch.batch.job.periodinspection.reader;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model.PeriodSettlementInspectionTarget;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.PerformancePointProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EmployeeProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EmployeeProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.QuantitativeEvaluationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class PeriodSettlementInspectionReader implements ItemReader<PeriodSettlementInspectionTarget> {

    private final EvaluationPeriodProjectionRepository evaluationPeriodProjectionRepository;
    private final EmployeeProjectionRepository employeeProjectionRepository;
    private final QuantitativeEvaluationRepository quantitativeEvaluationRepository;
    private final QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;
    private final PerformancePointProjectionRepository performancePointProjectionRepository;

    @Value("#{jobParameters['evaluationPeriodId']}")
    private Long requestedEvaluationPeriodId;

    @Value("#{jobParameters['periodType']}")
    private String requestedPeriodType;

    private Iterator<PeriodSettlementInspectionTarget> iterator = Collections.emptyIterator();
    private boolean initialized;

    @Override
    public PeriodSettlementInspectionTarget read() {
        if (!initialized) {
            initialized = true;
            iterator = loadItems().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<PeriodSettlementInspectionTarget> loadItems() {
        BatchPeriodType periodType = parsePeriodType(requestedPeriodType);
        if (!isUpperPeriod(periodType)) {
            log.info("Skipping period settlement inspection because periodType is not an upper period. periodType={}", periodType);
            return List.of();
        }

        EvaluationPeriodProjectionEntity targetPeriod = resolveTargetPeriod(periodType).orElse(null);
        if (targetPeriod == null) {
            log.info("Skipping period settlement inspection. No confirmed target period found. periodType={}, requestedEvaluationPeriodId={}", periodType, requestedEvaluationPeriodId);
            return List.of();
        }

        LocalDate startDate = targetPeriod.getStartDate();
        LocalDate endDate = targetPeriod.getEndDate();
        List<EvaluationPeriodProjectionEntity> monthlyPeriods = evaluationPeriodProjectionRepository.findConfirmedMonthlyPeriodsWithin(startDate, endDate);
        int expectedMonthCount = monthlyPeriods.size();

        if (expectedMonthCount == 0) {
            log.warn("No confirmed monthly settlements found inside target upper period. evaluationPeriodId={}, periodType={}", targetPeriod.getEvaluationPeriodId(), periodType);
            return List.of();
        }

        Map<Long, ScoreStats> quantitativeStats = buildQuantitativeStats(startDate, endDate);
        Map<Long, ScoreStats> qualitativeStats = buildQualitativeStats(startDate, endDate);
        Map<Long, BigDecimal> performancePointTotals = performancePointProjectionRepository
            .findEmployeePointTotalsByEarnedDateBetween(startDate, endDate)
            .stream()
            .collect(LinkedHashMap::new, (map, view) -> map.put(view.getEmployeeId(), safe(view.getTotalPoint())), LinkedHashMap::putAll);

        List<PeriodSettlementInspectionTarget> items = employeeProjectionRepository.findAll().stream()
            .filter(this::isEligibleEmployee)
            .map(employee -> toTarget(targetPeriod.getEvaluationPeriodId(), periodType, employee, expectedMonthCount, quantitativeStats, qualitativeStats, performancePointTotals))
            .filter(Objects::nonNull)
            .toList();

        log.info(
            "Loaded upper-period inspection targets. evaluationPeriodId={}, periodType={}, expectedMonthCount={}, employeeCount={}",
            targetPeriod.getEvaluationPeriodId(),
            periodType,
            expectedMonthCount,
            items.size()
        );
        return items;
    }

    private PeriodSettlementInspectionTarget toTarget(
        Long evaluationPeriodId,
        BatchPeriodType periodType,
        EmployeeProjectionEntity employee,
        int expectedMonthCount,
        Map<Long, ScoreStats> quantitativeStats,
        Map<Long, ScoreStats> qualitativeStats,
        Map<Long, BigDecimal> performancePointTotals
    ) {
        Long employeeId = employee.getEmployeeId();
        ScoreStats quant = quantitativeStats.getOrDefault(employeeId, ScoreStats.empty());
        ScoreStats qual = qualitativeStats.getOrDefault(employeeId, ScoreStats.empty());
        BigDecimal totalPoint = performancePointTotals.getOrDefault(employeeId, BigDecimal.ZERO);

        return PeriodSettlementInspectionTarget.builder()
            .evaluationPeriodId(evaluationPeriodId)
            .periodType(periodType)
            .employeeId(employeeId)
            .expectedMonthCount(expectedMonthCount)
            .quantitativeMonthCount(quant.count())
            .qualitativeMonthCount(qual.count())
            .quantitativeMinScore(quant.min())
            .quantitativeMaxScore(quant.max())
            .qualitativeMinScore(qual.min())
            .qualitativeMaxScore(qual.max())
            .performancePointTotal(totalPoint)
            .build();
    }

    private Map<Long, ScoreStats> buildQuantitativeStats(LocalDate startDate, LocalDate endDate) {
        Map<Long, ScoreStatsAccumulator> accumulators = new LinkedHashMap<>();
        for (QuantitativeEvaluationRepository.MonthlyQuantitativeInspectionRow row
            : quantitativeEvaluationRepository.findMonthlySettledInspectionRowsByPeriodRange(startDate, endDate)) {
            if (row.getEmployeeId() == null || row.getTScore() == null) {
                continue;
            }
            accumulators.computeIfAbsent(row.getEmployeeId(), ignored -> new ScoreStatsAccumulator()).add(row.getTScore());
        }
        return finish(accumulators);
    }

    private Map<Long, ScoreStats> buildQualitativeStats(LocalDate startDate, LocalDate endDate) {
        Map<Long, ScoreStatsAccumulator> accumulators = new LinkedHashMap<>();
        for (QualitativeScoreProjectionRepository.MonthlyQualitativeInspectionRow row
            : qualitativeScoreProjectionRepository.findMonthlyNormalizedInspectionRowsByPeriodRange(startDate, endDate)) {
            if (row.getEmployeeId() == null || row.getNormalizedScore() == null) {
                continue;
            }
            accumulators.computeIfAbsent(row.getEmployeeId(), ignored -> new ScoreStatsAccumulator()).add(row.getNormalizedScore());
        }
        return finish(accumulators);
    }

    private Map<Long, ScoreStats> finish(Map<Long, ScoreStatsAccumulator> accumulators) {
        Map<Long, ScoreStats> result = new LinkedHashMap<>();
        accumulators.forEach((employeeId, accumulator) -> result.put(employeeId, accumulator.finish()));
        return result;
    }

    private java.util.Optional<EvaluationPeriodProjectionEntity> resolveTargetPeriod(BatchPeriodType periodType) {
        if (requestedEvaluationPeriodId != null) {
            return evaluationPeriodProjectionRepository.findById(requestedEvaluationPeriodId);
        }
        return evaluationPeriodProjectionRepository.findLatestConfirmedPeriod(periodType);
    }

    private BatchPeriodType parsePeriodType(String value) {
        if (value == null || value.isBlank()) {
            return BatchPeriodType.QUARTER;
        }
        return BatchPeriodType.valueOf(value.trim().toUpperCase());
    }

    private boolean isUpperPeriod(BatchPeriodType value) {
        return value == BatchPeriodType.QUARTER
            || value == BatchPeriodType.HALF_YEAR
            || value == BatchPeriodType.YEAR;
    }

    private boolean isEligibleEmployee(EmployeeProjectionEntity employee) {
        if (employee.getEmployeeId() == null) {
            return false;
        }
        return employee.getEmployeeStatus() == null || !"INACTIVE".equalsIgnoreCase(employee.getEmployeeStatus());
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record ScoreStats(int count, BigDecimal min, BigDecimal max) {
        private static ScoreStats empty() {
            return new ScoreStats(0, null, null);
        }
    }

    private static final class ScoreStatsAccumulator {
        private int count;
        private BigDecimal min;
        private BigDecimal max;

        private void add(BigDecimal score) {
            if (score == null) {
                return;
            }
            count++;
            min = min == null || score.compareTo(min) < 0 ? score : min;
            max = max == null || score.compareTo(max) > 0 ? score : max;
        }

        private ScoreStats finish() {
            return new ScoreStats(count, min, max);
        }
    }
}
