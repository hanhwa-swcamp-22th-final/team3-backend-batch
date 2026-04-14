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
@Table(catalog = "batch_projection", name = "evaluation_weight_config_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationWeightConfigProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "evaluation_weight_config_id")
    private Long evaluationWeightConfigId;

    @Column(name = "tier_group", nullable = false)
    private String tierGroup;

    @Column(name = "category_code", nullable = false)
    private String categoryCode;

    @Column(name = "weight_percent", nullable = false)
    private Integer weightPercent;

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

    public static EvaluationWeightConfigProjectionEntity create(
        Long evaluationWeightConfigId,
        String tierGroup,
        String categoryCode,
        Integer weightPercent,
        Boolean active,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        EvaluationWeightConfigProjectionEntity entity = new EvaluationWeightConfigProjectionEntity();
        entity.evaluationWeightConfigId = evaluationWeightConfigId;
        entity.tierGroup = tierGroup;
        entity.categoryCode = categoryCode;
        entity.weightPercent = weightPercent;
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
        String tierGroup,
        String categoryCode,
        Integer weightPercent,
        Boolean active,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.tierGroup = tierGroup;
        this.categoryCode = categoryCode;
        this.weightPercent = weightPercent;
        this.active = active;
        this.deleted = deleted;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
