package com.ohgiraffers.team3backendbatch.api.command.dto;

/**
 * 수동 배치 실행 범위를 나타낸다.
 *
 * - FULL: 전체 대상 재계산
 * - PERIOD: 특정 평가 기간 재계산
 * - EMPLOYEE: 특정 직원 기준 부분 재계산
 */
public enum ManualJobLaunchMode {
    FULL,
    PERIOD,
    EMPLOYEE
}