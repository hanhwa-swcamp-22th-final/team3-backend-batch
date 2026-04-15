package com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KmsApprovedArticleCountRow {

    private Long employeeId;
    private Long approvedArticleCount;
}
