package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qualitative_evaluation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QualitativeEvaluationEntity {

    @Id
    @Column(name = "qualitative_evaluation_id")
    private Long qualitativeEvaluationId;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "s_qual")
    private BigDecimal sQual;

    @Column(name = "grade")
    private String grade;

    public void applyCalculatedResult(BigDecimal score, BigDecimal sQual, String grade) {
        this.score = score;
        this.sQual = sQual;
        this.grade = grade;
    }
}