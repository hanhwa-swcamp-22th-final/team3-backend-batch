package com.ohgiraffers.team3backendbatch.domain.scoring;

import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class MarkerPattern {

    private final String label;
    private final Pattern pattern;
}