package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "maintenance_item_standard_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceItemStandardProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "maintenance_item_standard_id")
    private Long maintenanceItemStandardId;

    @Column(name = "maintenance_item")
    private String maintenanceItem;

    @Column(name = "maintenance_weight")
    private BigDecimal maintenanceWeight;

    @Column(name = "maintenance_score_max")
    private BigDecimal maintenanceScoreMax;

    @Column(name = "deleted")
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

    public static MaintenanceItemStandardProjectionEntity create(
        Long maintenanceItemStandardId,
        String maintenanceItem,
        BigDecimal maintenanceWeight,
        BigDecimal maintenanceScoreMax,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        MaintenanceItemStandardProjectionEntity entity = new MaintenanceItemStandardProjectionEntity();
        entity.maintenanceItemStandardId = maintenanceItemStandardId;
        entity.refreshSnapshot(maintenanceItem, maintenanceWeight, maintenanceScoreMax, deleted, occurredAt, now);
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        String maintenanceItem,
        BigDecimal maintenanceWeight,
        BigDecimal maintenanceScoreMax,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.maintenanceItem = maintenanceItem;
        this.maintenanceWeight = maintenanceWeight;
        this.maintenanceScoreMax = maintenanceScoreMax;
        this.deleted = deleted;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
