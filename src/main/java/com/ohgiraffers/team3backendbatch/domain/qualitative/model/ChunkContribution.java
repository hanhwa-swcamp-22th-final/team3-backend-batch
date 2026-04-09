package com.ohgiraffers.team3backendbatch.domain.qualitative.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChunkContribution {

    private final BigDecimal chunkScore;
    private final boolean contrastive;
}