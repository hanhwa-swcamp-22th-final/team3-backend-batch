package com.ohgiraffers.team3backendbatch.domain.qualitative.model;

import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MarkerPattern {

    private final String label;
    private final Pattern pattern;
}