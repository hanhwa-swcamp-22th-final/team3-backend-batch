package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

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
@Table(name = "maintenance_log_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceLogProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "maintenance_log_id")
    private Long maintenanceLogId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "maintenance_item_standard_id", nullable = false)
    private Long maintenanceItemStandardId;

    @Column(name = "maintenance_type")
    private String maintenanceType;

    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;

    @Column(name = "maintenance_score")
    private BigDecimal maintenanceScore;

    @Column(name = "eta_maint_delta")
    private BigDecimal etaMaintDelta;

    @Column(name = "maintenance_result")
    private String maintenanceResult;

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

    public static MaintenanceLogProjectionEntity create(
        Long maintenanceLogId,
        Long equipmentId,
        Long maintenanceItemStandardId,
        String maintenanceType,
        LocalDate maintenanceDate,
        BigDecimal maintenanceScore,
        BigDecimal etaMaintDelta,
        String maintenanceResult,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        MaintenanceLogProjectionEntity entity = new MaintenanceLogProjectionEntity();
        entity.maintenanceLogId = maintenanceLogId;
        entity.refreshSnapshot(
            equipmentId,
            maintenanceItemStandardId,
            maintenanceType,
            maintenanceDate,
            maintenanceScore,
            etaMaintDelta,
            maintenanceResult,
            deleted,
            occurredAt,
            now
        );
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long equipmentId,
        Long maintenanceItemStandardId,
        String maintenanceType,
        LocalDate maintenanceDate,
        BigDecimal maintenanceScore,
        BigDecimal etaMaintDelta,
        String maintenanceResult,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.equipmentId = equipmentId;
        this.maintenanceItemStandardId = maintenanceItemStandardId;
        this.maintenanceType = maintenanceType;
        this.maintenanceDate = maintenanceDate;
        this.maintenanceScore = maintenanceScore;
        this.etaMaintDelta = etaMaintDelta;
        this.maintenanceResult = maintenanceResult;
        this.deleted = deleted;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
