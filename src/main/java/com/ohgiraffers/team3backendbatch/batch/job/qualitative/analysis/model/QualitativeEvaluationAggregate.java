package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.QualitativeKeywordRule;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QualitativeEvaluationAggregate {

    private final Long evaluationId;
    private final Long evaluationPeriodId;
    private final Long algorithmVersionId;
    private final Long employeeId;
    private final Long evaluatorId;
    private final Long evaluationLevel;
    private final SecondEvaluationMode secondEvaluationMode;
    private final BigDecimal baseRawScore;
    private final String commentText;
    private final String inputMethod;
    private final String analysisVersion;
    private final LocalDateTime submittedAt;
    private final List<QualitativeKeywordRule> keywordRules;
}