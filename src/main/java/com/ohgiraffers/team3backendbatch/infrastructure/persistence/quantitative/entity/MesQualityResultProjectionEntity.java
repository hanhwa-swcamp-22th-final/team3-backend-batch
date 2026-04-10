package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "mes_quality_result_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MesQualityResultProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "quality_result_id")
    private Long qualityResultId;

    @Column(name = "prod_lot_no")
    private String prodLotNo;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "source_equipment_code")
    private String sourceEquipmentCode;

    @Column(name = "input_lot_no")
    private String inputLotNo;

    @Column(name = "event_time_stamp")
    private LocalDateTime eventTimeStamp;

    @Column(name = "overall_result")
    private String overallResult;

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

    public static MesQualityResultProjectionEntity create(
        Long qualityResultId,
        String prodLotNo,
        Long equipmentId,
        String sourceEquipmentCode,
        String inputLotNo,
        LocalDateTime eventTimeStamp,
        String overallResult,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        MesQualityResultProjectionEntity entity = new MesQualityResultProjectionEntity();
        entity.qualityResultId = qualityResultId;
        entity.refreshSnapshot(
            prodLotNo,
            equipmentId,
            sourceEquipmentCode,
            inputLotNo,
            eventTimeStamp,
            overallResult,
            occurredAt,
            now
        );
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        String prodLotNo,
        Long equipmentId,
        String sourceEquipmentCode,
        String inputLotNo,
        LocalDateTime eventTimeStamp,
        String overallResult,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.prodLotNo = prodLotNo;
        this.equipmentId = equipmentId;
        this.sourceEquipmentCode = sourceEquipmentCode;
        this.inputLotNo = inputLotNo;
        this.eventTimeStamp = eventTimeStamp;
        this.overallResult = overallResult;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
