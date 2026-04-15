package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "tier_config_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TierConfigProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "tier_config_id")
    private Long tierConfigId;

    @Column(name = "tier_config_tier", nullable = false)
    private String tierConfigTier;

    @Column(name = "tier_config_weight_distribution")
    private String tierConfigWeightDistribution;

    @Column(name = "tier_config_promotion_point")
    private Integer tierConfigPromotionPoint;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted;

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

    public static TierConfigProjectionEntity create(
        Long tierConfigId,
        String tierConfigTier,
        String tierConfigWeightDistribution,
        Integer tierConfigPromotionPoint,
        Boolean active,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        TierConfigProjectionEntity entity = new TierConfigProjectionEntity();
        entity.tierConfigId = tierConfigId;
        entity.tierConfigTier = tierConfigTier;
        entity.tierConfigWeightDistribution = tierConfigWeightDistribution;
        entity.tierConfigPromotionPoint = tierConfigPromotionPoint;
        entity.active = active;
        entity.deleted = deleted;
        entity.occurredAt = occurredAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        String tierConfigTier,
        String tierConfigWeightDistribution,
        Integer tierConfigPromotionPoint,
        Boolean active,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.tierConfigTier = tierConfigTier;
        this.tierConfigWeightDistribution = tierConfigWeightDistribution;
        this.tierConfigPromotionPoint = tierConfigPromotionPoint;
        this.active = active;
        this.deleted = deleted;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
