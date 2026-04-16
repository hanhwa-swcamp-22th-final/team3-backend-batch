package com.ohgiraffers.team3backendbatch.batch.job.promotion.reader;

import com.ohgiraffers.team3backendbatch.batch.job.promotion.model.PromotionCandidateSnapshot;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.TierConfigProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.mapper.EmployeePerformancePointTotalRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.mapper.PerformancePointQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.mapper.PromotionHistoryQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.TierConfigProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EmployeeProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodProjectionRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.EvaluationPeriodQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EmployeeProjectionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class PromotionCandidateReader implements ItemReader<PromotionCandidateSnapshot> {

    private static final Logger log = LoggerFactory.getLogger(PromotionCandidateReader.class);
    private static final Set<String> PENDING_STATUSES = Set.of("UNDER_REVIEW", "CONFIRMATION_OF_PROMOTION");
    private static final BigDecimal PROMOTION_POINT_NORMALIZER = BigDecimal.valueOf(10_000);

    private final EmployeeProjectionRepository employeeProjectionRepository;
    private final EvaluationPeriodQueryMapper evaluationPeriodQueryMapper;
    private final TierConfigProjectionRepository tierConfigProjectionRepository;
    private final PerformancePointQueryMapper performancePointQueryMapper;
    private final PromotionHistoryQueryMapper promotionHistoryQueryMapper;

    private Iterator<PromotionCandidateSnapshot> iterator = Collections.emptyIterator();
    private boolean initialized;

    /**
     * 승급 후보 집계 대상을 한 건씩 반환한다.
     * @param 없음
     * @return 승급 후보 스냅샷 데이터
     */
    @Override
    public PromotionCandidateSnapshot read() {
        if (!initialized) {
            initialized = true;
            iterator = loadItems().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 승급 후보 스냅샷 목록을 생성한다.
     * @param 없음
     * @return 승급 후보 스냅샷 목록
     */
    private List<PromotionCandidateSnapshot> loadItems() {
        EvaluationPeriodProjectionRow evaluationPeriod = evaluationPeriodQueryMapper
            .findLatestConfirmedMonthlyPeriod()
            .orElse(null);

        if (evaluationPeriod == null) {
            log.info("Skipping promotion candidate job. No confirmed monthly evaluation period was found.");
            return List.of();
        }

        Map<String, TierConfigProjectionEntity> tierConfigByTier = new HashMap<>();
        for (TierConfigProjectionEntity projection : tierConfigProjectionRepository.findAllByActiveTrueAndDeletedFalse()) {
            if (projection.getTierConfigTier() != null) {
                tierConfigByTier.put(projection.getTierConfigTier().trim().toUpperCase(), projection);
            }
        }
        if (tierConfigByTier.isEmpty()) {
            log.info("Skipping promotion candidate job. No tier config projections were found.");
            return List.of();
        }

        Map<Long, BigDecimal> accumulatedPoints = new HashMap<>();
        for (EmployeePerformancePointTotalRow totalView : performancePointQueryMapper.findEmployeePointTotals()) {
            BigDecimal promotionPoint = totalView.getTotalPoint() == null
                ? BigDecimal.ZERO
                : totalView.getTotalPoint()
                    .divide(PROMOTION_POINT_NORMALIZER, 4, RoundingMode.HALF_UP);
            accumulatedPoints.put(totalView.getEmployeeId(), promotionPoint);
        }

        Set<Long> pendingEmployeeIds = new HashSet<>(promotionHistoryQueryMapper.findPendingEmployeeIds(PENDING_STATUSES));
        Long evaluationPeriodId = evaluationPeriod.getEvaluationPeriodId();
        LocalDate effectiveDate = evaluationPeriod.getEndDate();
        LocalDateTime occurredAt = LocalDateTime.now();

        List<PromotionCandidateSnapshot> items = employeeProjectionRepository.findAll().stream()
            .filter(this::isEligibleEmployee)
            .map(employee -> buildSnapshot(
                employee,
                evaluationPeriodId,
                effectiveDate,
                occurredAt,
                tierConfigByTier,
                accumulatedPoints,
                pendingEmployeeIds
            ))
            .filter(Objects::nonNull)
            .toList();

        log.info(
            "Prepared promotion candidate targets. evaluationPeriodId={}, employeeCount={}, pendingCount={}",
            evaluationPeriodId,
            items.size(),
            pendingEmployeeIds.size()
        );
        return items;
    }

    /**
     * 직원별 승급 후보 스냅샷을 생성한다.
     * @param employee 직원 projection 정보
     * @param evaluationPeriodId 평가 기간 ID
     * @param effectiveDate 기준 적용일
     * @param occurredAt 이벤트 발생 시각
     * @param tierConfigByTier 티어별 승급 기준 맵
     * @param accumulatedPoints 직원별 누적 포인트 맵
     * @param pendingEmployeeIds 보류 중인 승급 대상 직원 ID 집합
     * @return 승급 후보 스냅샷
     */
    private PromotionCandidateSnapshot buildSnapshot(
        EmployeeProjectionEntity employee,
        Long evaluationPeriodId,
        LocalDate effectiveDate,
        LocalDateTime occurredAt,
        Map<String, TierConfigProjectionEntity> tierConfigByTier,
        Map<Long, BigDecimal> accumulatedPoints,
        Set<Long> pendingEmployeeIds
    ) {
        if (pendingEmployeeIds.contains(employee.getEmployeeId())) {
            return null;
        }

        String currentTier = normalizeTier(employee.getEmployeeTier());
        if (currentTier == null) {
            return null;
        }

        String targetTier = resolveTargetTier(currentTier);
        if (targetTier == null) {
            return null;
        }

        TierConfigProjectionEntity currentTierConfig = tierConfigByTier.get(currentTier);
        TierConfigProjectionEntity targetTierConfig = tierConfigByTier.get(targetTier);
        if (currentTierConfig == null || targetTierConfig == null) {
            return null;
        }

        return PromotionCandidateSnapshot.builder()
            .employeeId(employee.getEmployeeId())
            .evaluationPeriodId(evaluationPeriodId)
            .periodType("MONTH")
            .currentTier(currentTier)
            .targetTier(targetTier)
            .currentTierConfigId(currentTierConfig.getTierConfigId())
            .targetTierConfigId(targetTierConfig.getTierConfigId())
            .tierAccumulatedPoint(accumulatedPoints.getOrDefault(employee.getEmployeeId(), BigDecimal.ZERO))
            .promotionThreshold(currentTierConfig.getTierConfigPromotionPoint())
            .tierConfigEffectiveDate(effectiveDate)
            .occurredAt(occurredAt)
            .build();
    }

    /**
     * 승급 후보 집계 대상 직원인지 확인한다.
     * @param employee 직원 projection 정보
     * @return 집계 대상 여부
     */
    private boolean isEligibleEmployee(EmployeeProjectionEntity employee) {
        if (employee.getEmployeeId() == null) {
            return false;
        }
        return employee.getEmployeeStatus() == null || !"INACTIVE".equalsIgnoreCase(employee.getEmployeeStatus());
    }

    /**
     * 직원 티어 문자열을 정규화한다.
     * @param employeeTier 직원 티어 문자열
     * @return 정규화된 티어 코드
     */
    private String normalizeTier(String employeeTier) {
        if (employeeTier == null || employeeTier.isBlank()) {
            return null;
        }
        String normalized = employeeTier.trim().toUpperCase();
        return switch (normalized) {
            case "S", "A", "B", "C" -> normalized;
            default -> null;
        };
    }

    /**
     * 현재 티어의 다음 승급 대상 티어를 반환한다.
     * @param currentTier 현재 티어
     * @return 승급 대상 티어
     */
    private String resolveTargetTier(String currentTier) {
        return switch (currentTier) {
            case "C" -> "B";
            case "B" -> "A";
            case "A" -> "S";
            default -> null;
        };
    }
}
