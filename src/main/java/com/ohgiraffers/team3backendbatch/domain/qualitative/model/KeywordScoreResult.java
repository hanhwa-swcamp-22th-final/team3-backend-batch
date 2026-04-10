package com.ohgiraffers.team3backendbatch.domain.qualitative.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeywordScoreResult {

    private final BigDecimal keywordWeightSum;
    private final int matchedKeywordCount;
    private final List<String> matchedKeywords;
    private final List<MatchedKeywordDetail> matchedKeywordDetails;
}
