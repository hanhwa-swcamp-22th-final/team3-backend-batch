package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "mes_quality_measurement_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MesQualityMeasurementProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @EmbeddedId
    private MesQualityMeasurementProjectionId id;

    @Column(name = "prod_lot_no")
    private String prodLotNo;

    @Column(name = "input_lot_no")
    private String inputLotNo;

    @Column(name = "ucl")
    private BigDecimal ucl;

    @Column(name = "target_value")
    private BigDecimal targetValue;

    @Column(name = "lcl")
    private BigDecimal lcl;

    @Column(name = "measured_value")
    private BigDecimal measuredValue;

    @Column(name = "judge_result")
    private String judgeResult;

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

    public static MesQualityMeasurementProjectionEntity create(
        Long qualityResultId,
        String processCode,
        String measureItem,
        String prodLotNo,
        String inputLotNo,
        BigDecimal ucl,
        BigDecimal targetValue,
        BigDecimal lcl,
        BigDecimal measuredValue,
        String judgeResult,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        MesQualityMeasurementProjectionEntity entity = new MesQualityMeasurementProjectionEntity();
        entity.id = new MesQualityMeasurementProjectionId(qualityResultId, processCode, measureItem);
        entity.refreshSnapshot(prodLotNo, inputLotNo, ucl, targetValue, lcl, measuredValue, judgeResult, occurredAt, now);
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        String prodLotNo,
        String inputLotNo,
        BigDecimal ucl,
        BigDecimal targetValue,
        BigDecimal lcl,
        BigDecimal measuredValue,
        String judgeResult,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.prodLotNo = prodLotNo;
        this.inputLotNo = inputLotNo;
        this.ucl = ucl;
        this.targetValue = targetValue;
        this.lcl = lcl;
        this.measuredValue = measuredValue;
        this.judgeResult = judgeResult;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
