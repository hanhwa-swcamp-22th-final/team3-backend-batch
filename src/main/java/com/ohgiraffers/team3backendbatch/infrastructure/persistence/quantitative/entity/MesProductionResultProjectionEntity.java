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
@Table(catalog = "batch_projection", name = "mes_production_result_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MesProductionResultProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "mes_production_result_projection_id")
    private Long mesProductionResultProjectionId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "input_qty")
    private BigDecimal inputQty;

    @Column(name = "good_qty")
    private BigDecimal goodQty;

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
        Long mesProductionResultProjectionId,
        Long equipmentId,
        LocalDate workDate,
        BigDecimal inputQty,
        BigDecimal goodQty,
        BigDecimal leadTimeSec,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        MesProductionResultProjectionEntity entity = new MesProductionResultProjectionEntity();
        entity.mesProductionResultProjectionId = mesProductionResultProjectionId;
        entity.refreshSnapshot(equipmentId, workDate, inputQty, goodQty, leadTimeSec, occurredAt, now);
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long equipmentId,
        LocalDate workDate,
        BigDecimal inputQty,
        BigDecimal goodQty,
        BigDecimal leadTimeSec,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.equipmentId = equipmentId;
        this.workDate = workDate;
        this.inputQty = inputQty;
        this.goodQty = goodQty;
        this.leadTimeSec = leadTimeSec;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
