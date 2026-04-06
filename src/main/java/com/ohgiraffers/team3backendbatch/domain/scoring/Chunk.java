package com.ohgiraffers.team3backendbatch.domain.scoring;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Chunk {

    private final String text;
    private final boolean contrastive;
}