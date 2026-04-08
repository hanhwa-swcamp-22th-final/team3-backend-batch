package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Qualitative analysis output model.
 */
@Getter
@Builder
@AllArgsConstructor
public class QualitativeAnalysisResult {

    private final Long evaluationId;
    private final Long evaluatorId;
    private final Long evaluationLevel;
    private final SecondEvaluationMode secondEvaluationMode;
    private final boolean reusedPreviousScore;
    private final BigDecimal baseRawScore;
    private final BigDecimal commentRawScore;
    private final BigDecimal commentSQual;
    private final BigDecimal adjustmentScore;
    private final BigDecimal squalRaw;
    private final BigDecimal originalSQual;
    private final BigDecimal sQual;
    private final String normalizedTier;
    private final int matchedKeywordCount;
    private final List<String> matchedKeywords;
    private final BigDecimal contextWeight;
    private final boolean negationDetected;
    private final String algorithmVersion;
    private final Long algorithmVersionId;
    private final String analysisStatus;
    private final LocalDateTime analyzedAt;
    private final boolean biasCorrected;
    private final String biasType;
    private final BigDecimal evaluatorAverage;
    private final BigDecimal companyAverage;
    private final BigDecimal alphaBias;
    private final List<QualitativeSentenceAnalysis> sentenceAnalyses;
}