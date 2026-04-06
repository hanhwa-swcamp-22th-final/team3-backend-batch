package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Raw NLP-based analysis result derived from a qualitative comment.
 */
@Getter
@Builder
@AllArgsConstructor
public class QualitativeCommentAnalysis {

    private final BigDecimal commentRawScore;
    private final BigDecimal officialRawScore;
    private final BigDecimal commentSQual;
    private final int matchedKeywordCount;
    private final List<String> matchedKeywords;
    private final BigDecimal contextWeight;
    private final boolean negationDetected;
}