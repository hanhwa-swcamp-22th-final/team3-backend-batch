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
@Table(catalog = "batch_projection", name = "mes_equipment_status_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MesEquipmentStatusProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "mes_equipment_status_projection_id")
    private Long mesEquipmentStatusProjectionId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "start_time_stamp")
    private LocalDateTime startTimeStamp;

    @Column(name = "downtime_minutes")
    private BigDecimal downtimeMinutes;

    @Column(name = "maintenance_minutes")
    private BigDecimal maintenanceMinutes;

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

    public static MesEquipmentStatusProjectionEntity create(
        Long mesEquipmentStatusProjectionId,
        Long equipmentId,
        LocalDateTime startTimeStamp,
        BigDecimal downtimeMinutes,
        BigDecimal maintenanceMinutes,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        MesEquipmentStatusProjectionEntity entity = new MesEquipmentStatusProjectionEntity();
        entity.mesEquipmentStatusProjectionId = mesEquipmentStatusProjectionId;
        entity.refreshSnapshot(equipmentId, startTimeStamp, downtimeMinutes, maintenanceMinutes, occurredAt, now);
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long equipmentId,
        LocalDateTime startTimeStamp,
        BigDecimal downtimeMinutes,
        BigDecimal maintenanceMinutes,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.equipmentId = equipmentId;
        this.startTimeStamp = startTimeStamp;
        this.downtimeMinutes = downtimeMinutes;
        this.maintenanceMinutes = maintenanceMinutes;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
