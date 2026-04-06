package com.ohgiraffers.team3backendbatch.api.query.dto;

import java.time.LocalDateTime;

public record BatchJobSummaryResponse(
    String jobName,
    Long latestExecutionId,
    String latestStatus,
    LocalDateTime latestStartTime,
    LocalDateTime latestEndTime
) {
}