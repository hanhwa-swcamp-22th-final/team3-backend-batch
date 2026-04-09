package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainKeywordRuleRow {

    private String keyword;
    private BigDecimal baseScore;
    private BigDecimal weight;
}