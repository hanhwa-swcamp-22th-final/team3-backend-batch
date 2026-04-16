package com.ohgiraffers.team3backendbatch.domain.qualitative.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Stores the detected marker position and raw marker text from a comment. */
@Getter
@AllArgsConstructor
public class MarkerMatch {

    private final int index;
    private final String marker;
}