package com.ohgiraffers.team3backendbatch.api.command.dto;

/**
 * 배치가 처리할 평가 기간 구분값이다.
 * 같은 잡 구조를 유지한 채 조회 기간과 계산 기준만 바꾸기 위해 사용한다.
 */
public enum BatchPeriodType {
    WEEK,
    MONTH,
    QUARTER,
    HALF_YEAR,
    YEAR
}