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
@Table(catalog = "batch_projection", name = "mes_production_result_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MesProductionResultProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "source_equipment_code")
    private String sourceEquipmentCode;

    @Column(name = "equipment_name_snapshot")
    private String equipmentNameSnapshot;

    @Column(name = "input_lot_no")
    private String inputLotNo;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "cycle_time_sec")
    private BigDecimal cycleTimeSec;

    @Column(name = "input_qty")
    private BigDecimal inputQty;

    @Column(name = "output_qty")
    private BigDecimal outputQty;

    @Column(name = "good_qty")
    private BigDecimal goodQty;

    @Column(name = "defect_qty")
    private BigDecimal defectQty;

    @Column(name = "lead_time_sec")
    private BigDecimal leadTimeSec;

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

    public static MesProductionResultProjectionEntity create(
        String eventId,
        Long equipmentId,
        String sourceEquipmentCode,
        String equipmentNameSnapshot,
        String inputLotNo,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal cycleTimeSec,
        BigDecimal inputQty,
        BigDecimal outputQty,
        BigDecimal goodQty,
        BigDecimal defectQty,
        BigDecimal leadTimeSec,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        MesProductionResultProjectionEntity entity = new MesProductionResultProjectionEntity();
        entity.eventId = eventId;
        entity.refreshSnapshot(
            equipmentId,
            sourceEquipmentCode,
            equipmentNameSnapshot,
            inputLotNo,
            startTime,
            endTime,
            cycleTimeSec,
            inputQty,
            outputQty,
            goodQty,
            defectQty,
            leadTimeSec,
            occurredAt,
            now
        );
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long equipmentId,
        String sourceEquipmentCode,
        String equipmentNameSnapshot,
        String inputLotNo,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal cycleTimeSec,
        BigDecimal inputQty,
        BigDecimal outputQty,
        BigDecimal goodQty,
        BigDecimal defectQty,
        BigDecimal leadTimeSec,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.equipmentId = equipmentId;
        this.sourceEquipmentCode = sourceEquipmentCode;
        this.equipmentNameSnapshot = equipmentNameSnapshot;
        this.inputLotNo = inputLotNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cycleTimeSec = cycleTimeSec;
        this.inputQty = inputQty;
        this.outputQty = outputQty;
        this.goodQty = goodQty;
        this.defectQty = defectQty;
        this.leadTimeSec = leadTimeSec;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
