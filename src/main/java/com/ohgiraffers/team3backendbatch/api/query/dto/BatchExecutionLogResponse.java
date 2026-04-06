package com.ohgiraffers.team3backendbatch.api.query.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO that exposes Spring Batch execution metadata in log-like form.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExecutionLogResponse {
    private String type;
    private String message;
    private String jobName;
    private Long executionId;
    private LocalDateTime occurredAt;
}