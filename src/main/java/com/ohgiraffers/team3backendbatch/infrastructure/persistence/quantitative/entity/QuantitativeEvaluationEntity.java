package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quantitative_evaluation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuantitativeEvaluationEntity {

    @Id
    @Column(name = "quantitative_evaluation_id")
    private Long quantitativeEvaluationId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "eval_period_id")
    private Long evaluationPeriodId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "uph_score")
    private BigDecimal uphScore;

    @Column(name = "yield_score")
    private BigDecimal yieldScore;

    @Column(name = "lead_time_score")
    private BigDecimal leadTimeScore;

    @Column(name = "actual_error")
    private BigDecimal actualError;

    @Column(name = "s_quant")
    private BigDecimal sQuant;

    @Column(name = "t_score")
    private BigDecimal tScore;

    @Column(name = "material_shielding")
    private Integer materialShielding;

    @Column(name = "status")
    private String status;

    public static QuantitativeEvaluationEntity create(
        Long quantitativeEvaluationId,
        Long employeeId,
        Long evaluationPeriodId,
        Long equipmentId
    ) {
        QuantitativeEvaluationEntity entity = new QuantitativeEvaluationEntity();
        entity.quantitativeEvaluationId = quantitativeEvaluationId;
        entity.employeeId = employeeId;
        entity.evaluationPeriodId = evaluationPeriodId;
        entity.equipmentId = equipmentId;
        return entity;
    }

    public void applyCalculatedResult(QuantitativeEvaluationAggregate aggregate) {
        this.uphScore = aggregate.getUphScore();
        this.yieldScore = aggregate.getYieldScore();
        this.leadTimeScore = aggregate.getLeadTimeScore();
        this.actualError = aggregate.getActualError();
        this.sQuant = aggregate.getSQuant();
        this.tScore = aggregate.getTScore();
        this.materialShielding = aggregate.getMaterialShielding() == null
            ? null
            : aggregate.getMaterialShielding().intValue();
        this.status = aggregate.getStatus();
    }
}