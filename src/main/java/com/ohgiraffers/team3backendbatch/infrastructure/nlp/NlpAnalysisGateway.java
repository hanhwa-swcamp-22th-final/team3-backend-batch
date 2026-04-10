package com.ohgiraffers.team3backendbatch.infrastructure.nlp;

import com.ohgiraffers.team3backendbatch.infrastructure.nlp.dto.NlpAnalysisResponse;

/**
 * External NLP analysis gateway.
 */
public interface NlpAnalysisGateway {

    NlpAnalysisResponse annotateText(String text);
}