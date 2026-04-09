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
@Table(catalog = "batch_projection", name = "mes_quality_result_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MesQualityResultProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "mes_quality_result_projection_id")
    private Long mesQualityResultProjectionId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "defect_qty")
    private BigDecimal defectQty;

    @Column(name = "actual_error")
    private BigDecimal actualError;

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
        Long mesQualityResultProjectionId,
        Long equipmentId,
        LocalDate workDate,
        BigDecimal defectQty,
        BigDecimal actualError,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        MesQualityResultProjectionEntity entity = new MesQualityResultProjectionEntity();
        entity.mesQualityResultProjectionId = mesQualityResultProjectionId;
        entity.refreshSnapshot(equipmentId, workDate, defectQty, actualError, occurredAt, now);
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long equipmentId,
        LocalDate workDate,
        BigDecimal defectQty,
        BigDecimal actualError,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.equipmentId = equipmentId;
        this.workDate = workDate;
        this.defectQty = defectQty;
        this.actualError = actualError;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
