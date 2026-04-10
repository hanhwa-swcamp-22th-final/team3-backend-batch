package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualitativeEvaluationSubmittedEvent {

    private Long qualitativeEvaluationId;
    private Long evaluationPeriodId;
    private Long algorithmVersionId;
    private Long evaluateeId;
    private Long evaluatorId;
    private Long evaluationLevel;
    private String secondEvaluationMode;
    private BigDecimal baseRawScore;
    private String evalComment;
    private String inputMethod;
    private String analysisVersion;
    private String status;
    private LocalDateTime occurredAt;
    private List<QualitativeKeywordRuleEvent> keywordRules;
}