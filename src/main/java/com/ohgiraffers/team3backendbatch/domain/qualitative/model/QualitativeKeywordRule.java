package com.ohgiraffers.team3backendbatch.domain.qualitative.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QualitativeKeywordRule {

    private final String keyword;
    private final BigDecimal scoreWeight;
}