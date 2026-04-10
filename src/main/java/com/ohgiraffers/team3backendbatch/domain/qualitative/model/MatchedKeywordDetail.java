package com.ohgiraffers.team3backendbatch.domain.qualitative.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchedKeywordDetail {

    private final Long domainKeywordId;
    private final String keyword;
    private final String domainCompetencyCategory;
    private final BigDecimal scoreWeight;
}
