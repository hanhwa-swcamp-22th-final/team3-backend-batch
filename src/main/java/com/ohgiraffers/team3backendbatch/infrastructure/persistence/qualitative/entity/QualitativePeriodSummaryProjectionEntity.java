package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qualitative_period_summary_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QualitativePeriodSummaryProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "qualitative_period_summary_id")
    private Long qualitativePeriodSummaryId;

    @Column(name = "evaluation_period_id", nullable = false)
    private Long evaluationPeriodId;

    @Column(name = "evaluatee_id", nullable = false)
    private Long evaluateeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private BatchPeriodType periodType;

    @Column(name = "source_month_count", nullable = false)
    private Integer sourceMonthCount;

    @Column(name = "average_raw_score")
    private BigDecimal averageRawScore;

    @Column(name = "average_normalized_score")
    private BigDecimal averageNormalizedScore;

    @Column(name = "grade", length = 10)
    private String grade;

    @Column(name = "summarized_at")
    private LocalDateTime summarizedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static QualitativePeriodSummaryProjectionEntity create(
        Long qualitativePeriodSummaryId,
        Long evaluationPeriodId,
        Long evaluateeId,
        BatchPeriodType periodType,
        int sourceMonthCount,
        BigDecimal averageRawScore,
        BigDecimal averageNormalizedScore,
        String grade,
        LocalDateTime summarizedAt,
        LocalDateTime now
    ) {
        QualitativePeriodSummaryProjectionEntity entity = new QualitativePeriodSummaryProjectionEntity();
        entity.qualitativePeriodSummaryId = qualitativePeriodSummaryId;
        entity.evaluationPeriodId = evaluationPeriodId;
        entity.evaluateeId = evaluateeId;
        entity.periodType = periodType;
        entity.sourceMonthCount = sourceMonthCount;
        entity.averageRawScore = averageRawScore;
        entity.averageNormalizedScore = averageNormalizedScore;
        entity.grade = grade;
        entity.summarizedAt = summarizedAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void applySummary(
        BatchPeriodType periodType,
        int sourceMonthCount,
        BigDecimal averageRawScore,
        BigDecimal averageNormalizedScore,
        String grade,
        LocalDateTime summarizedAt,
        LocalDateTime now
    ) {
        this.periodType = periodType;
        this.sourceMonthCount = sourceMonthCount;
        this.averageRawScore = averageRawScore;
        this.averageNormalizedScore = averageNormalizedScore;
        this.grade = grade;
        this.summarizedAt = summarizedAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
