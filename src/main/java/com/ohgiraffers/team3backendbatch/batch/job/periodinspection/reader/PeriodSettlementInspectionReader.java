package com.ohgiraffers.team3backendbatch.batch.job.periodinspection.reader;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model.PeriodSettlementInspectionTarget;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.mapper.PerformancePointQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.MonthlyQualitativeInspectionRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeScoreQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EmployeeProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodProjectionRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.MonthlyQuantitativeInspectionRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.QuantitativeEvaluationAggregateQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EmployeeProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
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
    private final EvaluationPeriodQueryMapper evaluationPeriodQueryMapper;
    private final EmployeeProjectionRepository employeeProjectionRepository;
    private final QuantitativeEvaluationAggregateQueryMapper quantitativeEvaluationAggregateQueryMapper;
    private final QualitativeScoreQueryMapper qualitativeScoreQueryMapper;
    private final PerformancePointQueryMapper performancePointQueryMapper;

    @Value("#{jobParameters['evaluationPeriodId']}")
    private Long requestedEvaluationPeriodId;

    @Value("#{jobParameters['periodType']}")
    private String requestedPeriodType;

    private Iterator<PeriodSettlementInspectionTarget> iterator = Collections.emptyIterator();
    private boolean initialized;

    /**
     * 상위 기간 정산 점검 대상 데이터를 한 건씩 반환한다.
     * @param 없음
     * @return 상위 기간 정산 점검 대상 데이터
     */
    @Override
    public PeriodSettlementInspectionTarget read() {
        if (!initialized) {
            initialized = true;
            iterator = loadItems().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 상위 기간 정산 점검 대상 목록을 조회한다.
     * @param 없음
     * @return 상위 기간 정산 점검 대상 목록
     */
    private List<PeriodSettlementInspectionTarget> loadItems() {
        BatchPeriodType periodType = parsePeriodType(requestedPeriodType);
        if (!isUpperPeriod(periodType)) {
            log.info("Skipping period settlement inspection because periodType is not an upper period. periodType={}", periodType);
            return List.of();
        }

        EvaluationPeriodProjectionRow targetPeriod = resolveTargetPeriod(periodType).orElse(null);
        if (targetPeriod == null) {
            log.info("Skipping period settlement inspection. No confirmed target period found. periodType={}, requestedEvaluationPeriodId={}", periodType, requestedEvaluationPeriodId);
            return List.of();
        }

        LocalDate startDate = targetPeriod.getStartDate();
        LocalDate endDate = targetPeriod.getEndDate();
        List<EvaluationPeriodProjectionRow> monthlyPeriods = evaluationPeriodQueryMapper.findConfirmedMonthlyPeriodsWithin(startDate, endDate);
        int expectedMonthCount = monthlyPeriods.size();

        if (expectedMonthCount == 0) {
            log.warn("No confirmed monthly settlements found inside target upper period. evaluationPeriodId={}, periodType={}", targetPeriod.getEvaluationPeriodId(), periodType);
            return List.of();
        }

        Map<Long, ScoreStats> quantitativeStats = buildQuantitativeStats(startDate, endDate);
        Map<Long, ScoreStats> qualitativeStats = buildQualitativeStats(startDate, endDate);
        Map<Long, BigDecimal> performancePointTotals = performancePointQueryMapper
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

    /**
     * 직원별 정산 점검 대상 객체를 생성한다.
     * @param evaluationPeriodId 평가 기간 ID
     * @param periodType 평가 기간 유형
     * @param employee 직원 projection 정보
     * @param expectedMonthCount 기대 월 수
     * @param quantitativeStats 정량 점수 통계
     * @param qualitativeStats 정성 점수 통계
     * @param performancePointTotals 성과 포인트 합계
     * @return 상위 기간 정산 점검 대상 객체
     */
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

    /**
     * 월간 정량 점수 통계를 생성한다.
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 직원별 정량 점수 통계
     */
    private Map<Long, ScoreStats> buildQuantitativeStats(LocalDate startDate, LocalDate endDate) {
        Map<Long, ScoreStatsAccumulator> accumulators = new LinkedHashMap<>();
        for (MonthlyQuantitativeInspectionRow row
            : quantitativeEvaluationAggregateQueryMapper.findMonthlySettledInspectionRowsByPeriodRange(startDate, endDate)) {
            if (row.getEmployeeId() == null || row.getTScore() == null) {
                continue;
            }
            accumulators.computeIfAbsent(row.getEmployeeId(), ignored -> new ScoreStatsAccumulator()).add(row.getTScore());
        }
        return finish(accumulators);
    }

    /**
     * 월간 정성 점수 통계를 생성한다.
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 직원별 정성 점수 통계
     */
    private Map<Long, ScoreStats> buildQualitativeStats(LocalDate startDate, LocalDate endDate) {
        Map<Long, ScoreStatsAccumulator> accumulators = new LinkedHashMap<>();
        for (MonthlyQualitativeInspectionRow row
            : qualitativeScoreQueryMapper.findMonthlyNormalizedInspectionRowsByPeriodRange(startDate, endDate)) {
            if (row.getEmployeeId() == null || row.getNormalizedScore() == null) {
                continue;
            }
            accumulators.computeIfAbsent(row.getEmployeeId(), ignored -> new ScoreStatsAccumulator()).add(row.getNormalizedScore());
        }
        return finish(accumulators);
    }

    /**
     * 누적 통계 값을 최종 통계 값으로 변환한다.
     * @param accumulators 직원별 누적 통계 맵
     * @return 직원별 최종 점수 통계
     */
    private Map<Long, ScoreStats> finish(Map<Long, ScoreStatsAccumulator> accumulators) {
        Map<Long, ScoreStats> result = new LinkedHashMap<>();
        accumulators.forEach((employeeId, accumulator) -> result.put(employeeId, accumulator.finish()));
        return result;
    }

    /**
     * 점검 대상 평가 기간을 조회한다.
     * @param periodType 평가 기간 유형
     * @return 평가 기간 조회 결과
     */
    private java.util.Optional<EvaluationPeriodProjectionRow> resolveTargetPeriod(BatchPeriodType periodType) {
        if (requestedEvaluationPeriodId != null) {
            return evaluationPeriodProjectionRepository.findById(requestedEvaluationPeriodId).map(this::toPeriodRow);
        }
        return evaluationPeriodQueryMapper.findLatestConfirmedPeriod(periodType);
    }

    /**
     * 문자열 periodType 값을 배치 enum 으로 변환한다.
     * @param value 요청된 periodType 문자열
     * @return 배치 평가 기간 유형
     */
    private BatchPeriodType parsePeriodType(String value) {
        if (value == null || value.isBlank()) {
            return BatchPeriodType.QUARTER;
        }
        return BatchPeriodType.valueOf(value.trim().toUpperCase());
    }

    /**
     * 상위 기간 점검 대상인지 확인한다.
     * @param value 평가 기간 유형
     * @return 상위 기간 여부
     */
    private boolean isUpperPeriod(BatchPeriodType value) {
        return value == BatchPeriodType.QUARTER
            || value == BatchPeriodType.HALF_YEAR
            || value == BatchPeriodType.YEAR;
    }

    /**
     * 배치 처리 대상 직원인지 확인한다.
     * @param employee 직원 projection 정보
     * @return 처리 대상 여부
     */
    private boolean isEligibleEmployee(EmployeeProjectionEntity employee) {
        if (employee.getEmployeeId() == null) {
            return false;
        }
        return employee.getEmployeeStatus() == null || !"INACTIVE".equalsIgnoreCase(employee.getEmployeeStatus());
    }

    /**
     * null 점수를 0으로 치환한다.
     * @param value 점수 값
     * @return 보정된 점수 값
     */
    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 평가 기간 projection entity 를 mapper row 형태로 변환한다.
     * @param entity 평가 기간 projection entity
     * @return mapper 조회용 평가 기간 row
     */
    private EvaluationPeriodProjectionRow toPeriodRow(EvaluationPeriodProjectionEntity entity) {
        EvaluationPeriodProjectionRow row = new EvaluationPeriodProjectionRow();
        row.setEvaluationPeriodId(entity.getEvaluationPeriodId());
        row.setAlgorithmVersionId(entity.getAlgorithmVersionId());
        row.setEvaluationYear(entity.getEvaluationYear());
        row.setEvaluationSequence(entity.getEvaluationSequence());
        row.setStartDate(entity.getStartDate());
        row.setEndDate(entity.getEndDate());
        row.setStatus(entity.getStatus());
        row.setPolicyConfig(entity.getPolicyConfig());
        row.setParameters(entity.getParameters());
        row.setReferenceValues(entity.getReferenceValues());
        return row;
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

        /**
         * 점수를 누적 통계에 반영한다.
         * @param score 누적할 점수
         * @return 반환값 없음
         */
        private void add(BigDecimal score) {
            if (score == null) {
                return;
            }
            count++;
            min = min == null || score.compareTo(min) < 0 ? score : min;
            max = max == null || score.compareTo(max) > 0 ? score : max;
        }

        /**
         * 누적된 점수 통계를 완료한다.
         * @param 없음
         * @return 최종 점수 통계
         */
        private ScoreStats finish() {
            return new ScoreStats(count, min, max);
        }
    }
}
