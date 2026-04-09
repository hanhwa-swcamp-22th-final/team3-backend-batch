package com.ohgiraffers.team3backendbatch.domain.qualitative.scoring;

import com.ohgiraffers.team3backendbatch.domain.qualitative.model.QualitativeKeywordRule;
import java.util.List;

@FunctionalInterface
public interface QualitativeKeywordRuleLoader {

    /**
     * Returns active keyword rules loaded from the authoritative source.
     * Returns null when the external source is temporarily unavailable.
     */
    List<QualitativeKeywordRule> loadActiveKeywordRules();
}