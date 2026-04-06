package com.ohgiraffers.team3backendbatch.api.query.dto;

import java.time.LocalDateTime;

/**
 * 배치 실행 이력 요약을 로그 형태로 내려주는 응답 DTO이다.
 * 외부 로그 시스템이 아니라 Spring Batch 실행 메타데이터 기반 응답을 표현한다.
 */
public record BatchExecutionLogResponse(
    String type,
    String message,
    String jobName,
    Long executionId,
    LocalDateTime occurredAt
) {
}