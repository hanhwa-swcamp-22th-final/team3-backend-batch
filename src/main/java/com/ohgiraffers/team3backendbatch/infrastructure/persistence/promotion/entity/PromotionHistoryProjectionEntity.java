package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "promotion_history_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionHistoryProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "tier_promotion_id")
    private Long tierPromotionId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "current_tier_config_id", nullable = false)
    private Long currentTierConfigId;

    @Column(name = "target_tier_config_id", nullable = false)
    private Long targetTierConfigId;

    @Column(name = "tier_config_effective_date")
    private LocalDate tierConfigEffectiveDate;

    @Column(name = "tier_accumulated_point")
    private BigDecimal tierAccumulatedPoint;

    @Column(name = "tier_promo_status", nullable = false)
    private String tierPromoStatus;

    @Column(name = "tier_reviewed_at")
    private LocalDateTime tierReviewedAt;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static PromotionHistoryProjectionEntity create(
        Long tierPromotionId,
        Long employeeId,
        Long reviewerId,
        Long currentTierConfigId,
        Long targetTierConfigId,
        LocalDate tierConfigEffectiveDate,
        BigDecimal tierAccumulatedPoint,
        String tierPromoStatus,
        LocalDateTime tierReviewedAt,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        PromotionHistoryProjectionEntity entity = new PromotionHistoryProjectionEntity();
        entity.tierPromotionId = tierPromotionId;
        entity.employeeId = employeeId;
        entity.reviewerId = reviewerId;
        entity.currentTierConfigId = currentTierConfigId;
        entity.targetTierConfigId = targetTierConfigId;
        entity.tierConfigEffectiveDate = tierConfigEffectiveDate;
        entity.tierAccumulatedPoint = tierAccumulatedPoint;
        entity.tierPromoStatus = tierPromoStatus;
        entity.tierReviewedAt = tierReviewedAt;
        entity.occurredAt = occurredAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long employeeId,
        Long reviewerId,
        Long currentTierConfigId,
        Long targetTierConfigId,
        LocalDate tierConfigEffectiveDate,
        BigDecimal tierAccumulatedPoint,
        String tierPromoStatus,
        LocalDateTime tierReviewedAt,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.employeeId = employeeId;
        this.reviewerId = reviewerId;
        this.currentTierConfigId = currentTierConfigId;
        this.targetTierConfigId = targetTierConfigId;
        this.tierConfigEffectiveDate = tierConfigEffectiveDate;
        this.tierAccumulatedPoint = tierAccumulatedPoint;
        this.tierPromoStatus = tierPromoStatus;
        this.tierReviewedAt = tierReviewedAt;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
