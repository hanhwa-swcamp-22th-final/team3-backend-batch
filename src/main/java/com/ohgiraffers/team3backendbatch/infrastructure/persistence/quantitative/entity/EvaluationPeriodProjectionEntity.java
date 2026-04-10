package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "evaluation_period_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationPeriodProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "evaluation_period_id")
    private Long evaluationPeriodId;

    @Column(name = "algorithm_version_id")
    private Long algorithmVersionId;

    @Column(name = "evaluation_year", nullable = false)
    private Integer evaluationYear;

    @Column(name = "evaluation_sequence", nullable = false)
    private Integer evaluationSequence;

    @Column(name = "evaluation_type")
    private String evaluationType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status")
    private String status;

    @Column(name = "algorithm_version_no")
    private String algorithmVersionNo;

    @Column(name = "algorithm_implementation_key")
    private String algorithmImplementationKey;

    @Column(name = "parameters")
    private String parameters;

    @Column(name = "reference_values")
    private String referenceValues;

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

    public static EvaluationPeriodProjectionEntity create(
        Long evaluationPeriodId,
        Long algorithmVersionId,
        Integer evaluationYear,
        Integer evaluationSequence,
        String evaluationType,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String algorithmVersionNo,
        String algorithmImplementationKey,
        String parameters,
        String referenceValues,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        EvaluationPeriodProjectionEntity entity = new EvaluationPeriodProjectionEntity();
        entity.evaluationPeriodId = evaluationPeriodId;
        entity.algorithmVersionId = algorithmVersionId;
        entity.evaluationYear = evaluationYear;
        entity.evaluationSequence = evaluationSequence;
        entity.evaluationType = evaluationType;
        entity.startDate = startDate;
        entity.endDate = endDate;
        entity.status = status;
        entity.algorithmVersionNo = algorithmVersionNo;
        entity.algorithmImplementationKey = algorithmImplementationKey;
        entity.parameters = parameters;
        entity.referenceValues = referenceValues;
        entity.occurredAt = occurredAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long algorithmVersionId,
        Integer evaluationYear,
        Integer evaluationSequence,
        String evaluationType,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String algorithmVersionNo,
        String algorithmImplementationKey,
        String parameters,
        String referenceValues,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.algorithmVersionId = algorithmVersionId;
        this.evaluationYear = evaluationYear;
        this.evaluationSequence = evaluationSequence;
        this.evaluationType = evaluationType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.algorithmVersionNo = algorithmVersionNo;
        this.algorithmImplementationKey = algorithmImplementationKey;
        this.parameters = parameters;
        this.referenceValues = referenceValues;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
