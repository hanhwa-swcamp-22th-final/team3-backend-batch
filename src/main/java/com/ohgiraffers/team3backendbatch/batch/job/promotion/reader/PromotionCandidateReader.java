package com.ohgiraffers.team3backendbatch.batch.job.promotion.reader;

import com.ohgiraffers.team3backendbatch.batch.job.promotion.model.PromotionCandidateSnapshot;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.TierConfigProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.PerformancePointProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.PromotionHistoryProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.TierConfigProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EmployeeProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EmployeeProjectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.EvaluationPeriodProjectionRepository;
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
    private final EvaluationPeriodProjectionRepository evaluationPeriodProjectionRepository;
    private final TierConfigProjectionRepository tierConfigProjectionRepository;
    private final PerformancePointProjectionRepository performancePointProjectionRepository;
    private final PromotionHistoryProjectionRepository promotionHistoryProjectionRepository;

    private Iterator<PromotionCandidateSnapshot> iterator = Collections.emptyIterator();
    private boolean initialized;

    @Override
    public PromotionCandidateSnapshot read() {
        if (!initialized) {
            initialized = true;
            iterator = loadItems().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<PromotionCandidateSnapshot> loadItems() {
        EvaluationPeriodProjectionEntity evaluationPeriod = evaluationPeriodProjectionRepository
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
        for (PerformancePointProjectionRepository.EmployeePerformancePointTotalView totalView : performancePointProjectionRepository.findEmployeePointTotals()) {
            BigDecimal promotionPoint = totalView.getTotalPoint() == null
                ? BigDecimal.ZERO
                : totalView.getTotalPoint()
                    .divide(PROMOTION_POINT_NORMALIZER, 4, RoundingMode.HALF_UP);
            accumulatedPoints.put(totalView.getEmployeeId(), promotionPoint);
        }

        Set<Long> pendingEmployeeIds = new HashSet<>(promotionHistoryProjectionRepository.findPendingEmployeeIds(PENDING_STATUSES));
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

    private boolean isEligibleEmployee(EmployeeProjectionEntity employee) {
        if (employee.getEmployeeId() == null) {
            return false;
        }
        return employee.getEmployeeStatus() == null || !"INACTIVE".equalsIgnoreCase(employee.getEmployeeStatus());
    }

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

    private String resolveTargetTier(String currentTier) {
        return switch (currentTier) {
            case "C" -> "B";
            case "B" -> "A";
            case "A" -> "S";
            default -> null;
        };
    }
}
