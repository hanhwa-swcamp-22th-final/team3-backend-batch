package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evaluation_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationCommentEntity {

    @Id
    @Column(name = "evaluation_comment_id")
    private Long evaluationCommentId;

    @Column(name = "qualitative_evaluation_id", nullable = false)
    private Long qualitativeEvaluationId;

    @Column(name = "matched_keywords", columnDefinition = "JSON")
    private String matchedKeywords;

    @Column(name = "matched_keyword_details", columnDefinition = "JSON")
    private String matchedKeywordDetails;
}
