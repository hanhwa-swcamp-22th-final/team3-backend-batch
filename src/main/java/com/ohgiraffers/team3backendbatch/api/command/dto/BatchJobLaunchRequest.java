package com.ohgiraffers.team3backendbatch.api.command.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 수동 배치 실행 요청 스켈레톤이다.
 *
 * 예상 사용 방식:
 * - mode + periodType + evaluationPeriodId 조합으로 대상 기간 결정
 * - EMPLOYEE 모드에서는 employeeId를 함께 전달
 * - force=true면 같은 기간/같은 대상의 중복 실행 제약을 예외 처리할 수 있다.
 */
public record BatchJobLaunchRequest(
    ManualJobLaunchMode mode,
    BatchPeriodType periodType,
    Long evaluationPeriodId,
    Long employeeId,
    Boolean force,
    @NotBlank String requestedBy,
    @NotBlank String reason
) {
}