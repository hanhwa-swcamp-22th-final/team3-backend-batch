package com.ohgiraffers.team3backendbatch.infrastructure.persistence.bias.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bias_correction")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BiasCorrectionEntity {

    @Id
    @Column(name = "bias_correction_id")
    private Long biasCorrectionId;

    @Column(name = "evaluator_id", nullable = false)
    private Long evaluatorId;

    @Column(name = "qualitative_evaluation_id", nullable = false)
    private Long qualitativeEvaluationId;

    @Column(name = "bias_type", nullable = false)
    private String biasType;

    @Column(name = "evaluator_avg", nullable = false)
    private BigDecimal evaluatorAvg;

    @Column(name = "company_avg", nullable = false)
    private BigDecimal companyAvg;

    @Column(name = "alpha_bias", nullable = false)
    private BigDecimal alphaBias;

    @Column(name = "original_score", nullable = false)
    private BigDecimal originalScore;

    @Column(name = "corrected_score", nullable = false)
    private BigDecimal correctedScore;

    @Column(name = "notification_sent", nullable = false)
    private Boolean notificationSent;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;
}