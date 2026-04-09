package com.ohgiraffers.team3backendbatch.api.command.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobLaunchResponse {
    private Long executionId;
    private String jobName;
    private String status;
    private Map<String, String> parameters;
    private LocalDateTime launchedAt;
}