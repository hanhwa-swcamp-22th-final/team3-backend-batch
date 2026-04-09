package com.ohgiraffers.team3backendbatch.infrastructure.nlp.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Internal NLP response model used by the batch module.
 */
@Getter
@AllArgsConstructor
public class NlpAnalysisResponse {

    private final BigDecimal sentimentScore;
    private final boolean negationDetected;
    private final List<String> keywordLemmas;
}