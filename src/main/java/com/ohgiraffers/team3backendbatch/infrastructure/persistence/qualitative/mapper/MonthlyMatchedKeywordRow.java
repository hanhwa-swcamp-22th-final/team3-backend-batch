package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MonthlyMatchedKeywordRow {

    private Long employeeId;
    private String matchedKeywords;
    private String matchedKeywordDetails;
}
