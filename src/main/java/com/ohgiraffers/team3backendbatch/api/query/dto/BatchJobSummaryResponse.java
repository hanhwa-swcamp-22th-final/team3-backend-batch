package com.ohgiraffers.team3backendbatch.api.query.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobSummaryResponse {
    private String jobName;
    private Long latestExecutionId;
    private String latestStatus;
    private LocalDateTime latestStartTime;
    private LocalDateTime latestEndTime;
}