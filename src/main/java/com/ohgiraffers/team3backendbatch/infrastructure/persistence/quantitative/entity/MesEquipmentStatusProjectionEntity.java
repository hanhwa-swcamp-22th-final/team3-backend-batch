package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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

    @EmbeddedId
    private MesEquipmentStatusProjectionId id;

    @Column(name = "source_equipment_code")
    private String sourceEquipmentCode;

    @Column(name = "end_time_stamp")
    private LocalDateTime endTimeStamp;

    @Column(name = "alarm_code")
    private String alarmCode;

    @Column(name = "alarm_desc")
    private String alarmDesc;

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
        Long equipmentId,
        String statusType,
        LocalDateTime startTimeStamp,
        String sourceEquipmentCode,
        LocalDateTime endTimeStamp,
        String alarmCode,
        String alarmDesc,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        MesEquipmentStatusProjectionEntity entity = new MesEquipmentStatusProjectionEntity();
        entity.id = new MesEquipmentStatusProjectionId(equipmentId, statusType, startTimeStamp);
        entity.refreshSnapshot(sourceEquipmentCode, endTimeStamp, alarmCode, alarmDesc, occurredAt, now);
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        String sourceEquipmentCode,
        LocalDateTime endTimeStamp,
        String alarmCode,
        String alarmDesc,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.sourceEquipmentCode = sourceEquipmentCode;
        this.endTimeStamp = endTimeStamp;
        this.alarmCode = alarmCode;
        this.alarmDesc = alarmDesc;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
