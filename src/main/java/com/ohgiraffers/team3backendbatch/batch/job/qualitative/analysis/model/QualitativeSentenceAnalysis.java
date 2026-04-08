package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Sentence-level qualitative analysis metadata.
 */
@Getter
@Builder
@AllArgsConstructor
public class QualitativeSentenceAnalysis {

    private final int sentenceOrder;
    private final boolean contrastive;
    private final BigDecimal nlpSentiment;
    private final int matchedKeywordCount;
    private final List<String> matchedKeywords;
    private final BigDecimal contextWeight;
    private final boolean negationDetected;
}