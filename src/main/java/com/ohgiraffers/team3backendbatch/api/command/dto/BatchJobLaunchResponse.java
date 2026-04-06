package com.ohgiraffers.team3backendbatch.api.command.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record BatchJobLaunchResponse(
    Long executionId,
    String jobName,
    String status,
    Map<String, String> parameters,
    LocalDateTime launchedAt
) {
}