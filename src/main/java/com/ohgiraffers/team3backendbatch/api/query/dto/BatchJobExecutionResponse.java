package com.ohgiraffers.team3backendbatch.api.query.dto;

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
public class BatchJobExecutionResponse {
    private Long executionId;
    private String jobName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, String> parameters;
}