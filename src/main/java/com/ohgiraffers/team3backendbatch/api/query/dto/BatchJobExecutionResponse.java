package com.ohgiraffers.team3backendbatch.api.query.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record BatchJobExecutionResponse(
    Long executionId,
    String jobName,
    String status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Map<String, String> parameters
) {
}