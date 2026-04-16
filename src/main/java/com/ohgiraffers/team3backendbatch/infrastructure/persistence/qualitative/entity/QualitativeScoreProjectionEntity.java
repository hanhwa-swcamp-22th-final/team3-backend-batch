package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity;

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
@Table(catalog = "batch_projection", name = "qualitative_score_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QualitativeScoreProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "qualitative_evaluation_id")
    private Long qualitativeEvaluationId;

    @Column(name = "evaluation_period_id", nullable = false)
    private Long evaluationPeriodId;

    @Column(name = "evaluatee_id", nullable = false)
    private Long evaluateeId;

    @Column(name = "evaluator_id")
    private Long evaluatorId;

    @Column(name = "evaluation_level", nullable = false)
    private Long evaluationLevel;

    @Column(name = "algorithm_version_id")
    private Long algorithmVersionId;

    @Column(name = "analysis_version")
    private String analysisVersion;

    @Column(name = "analysis_status")
    private String analysisStatus;

    @Column(name = "raw_score")
    private BigDecimal rawScore;

    @Column(name = "normalized_score")
    private BigDecimal normalizedScore;

    @Column(name = "grade")
    private String grade;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "normalized_at")
    private LocalDateTime normalizedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static QualitativeScoreProjectionEntity create(
        Long qualitativeEvaluationId,
        Long evaluationPeriodId,
        Long evaluateeId,
        Long evaluatorId,
        Long evaluationLevel,
        Long algorithmVersionId,
        String analysisVersion,
        String analysisStatus,
        LocalDateTime submittedAt,
        LocalDateTime now
    ) {
        QualitativeScoreProjectionEntity entity = new QualitativeScoreProjectionEntity();
        entity.qualitativeEvaluationId = qualitativeEvaluationId;
        entity.evaluationPeriodId = evaluationPeriodId;
        entity.evaluateeId = evaluateeId;
        entity.evaluatorId = evaluatorId;
        entity.evaluationLevel = evaluationLevel;
        entity.algorithmVersionId = algorithmVersionId;
        entity.analysisVersion = analysisVersion;
        entity.analysisStatus = analysisStatus;
        entity.submittedAt = submittedAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSubmittedSnapshot(
        Long evaluationPeriodId,
        Long evaluateeId,
        Long evaluatorId,
        Long evaluationLevel,
        Long algorithmVersionId,
        String analysisVersion,
        String analysisStatus,
        LocalDateTime submittedAt,
        LocalDateTime now
    ) {
        this.evaluationPeriodId = evaluationPeriodId;
        this.evaluateeId = evaluateeId;
        this.evaluatorId = evaluatorId;
        this.evaluationLevel = evaluationLevel;
        this.algorithmVersionId = algorithmVersionId;
        this.analysisVersion = analysisVersion;
        this.analysisStatus = analysisStatus;
        this.submittedAt = submittedAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }

    public void recordAnalysis(BigDecimal rawScore, String analysisStatus, LocalDateTime analyzedAt, LocalDateTime now) {
        this.rawScore = rawScore;
        this.analysisStatus = analysisStatus;
        this.analyzedAt = analyzedAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }

    public void recordNormalization(BigDecimal normalizedScore, String grade, LocalDateTime normalizedAt, LocalDateTime now) {
        this.normalizedScore = normalizedScore;
        this.grade = grade;
        this.normalizedAt = normalizedAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
