package com.ohgiraffers.team3backendbatch.batch.common.launcher;

import com.ohgiraffers.team3backendbatch.api.query.dto.BatchExecutionLogResponse;
import com.ohgiraffers.team3backendbatch.api.query.dto.BatchJobExecutionResponse;
import com.ohgiraffers.team3backendbatch.api.query.dto.BatchJobSummaryResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchJobQueryFacade {

    private final JobExplorer jobExplorer;

    public List<BatchJobSummaryResponse> getJobSummaries() {
        List<BatchJobSummaryResponse> responses = new ArrayList<>();

        for (String jobName : jobExplorer.getJobNames()) {
            List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, 1);
            JobExecution latestExecution = instances.stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .max(Comparator.comparing(JobExecution::getCreateTime,
                    Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

            responses.add(BatchJobSummaryResponse.builder()
                .jobName(jobName)
                .latestExecutionId(latestExecution != null ? latestExecution.getId() : null)
                .latestStatus(latestExecution != null ? latestExecution.getStatus().name() : "NEVER_RUN")
                .latestStartTime(latestExecution != null ? latestExecution.getStartTime() : null)
                .latestEndTime(latestExecution != null ? latestExecution.getEndTime() : null)
                .build());
        }

        responses.sort(Comparator.comparing(BatchJobSummaryResponse::getJobName));
        return responses;
    }

    public BatchJobExecutionResponse getExecution(Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);

        if (execution == null) {
            throw new IllegalArgumentException("Unknown executionId: " + executionId);
        }

        return BatchJobExecutionResponse.builder()
            .executionId(execution.getId())
            .jobName(execution.getJobInstance().getJobName())
            .status(execution.getStatus().name())
            .startTime(execution.getStartTime())
            .endTime(execution.getEndTime())
            .parameters(execution.getJobParameters().getParameters().entrySet().stream()
                .collect(LinkedHashMap::new,
                    (map, entry) -> map.put(entry.getKey(), String.valueOf(entry.getValue().getValue())),
                    LinkedHashMap::putAll))
            .build();
    }

    public List<BatchExecutionLogResponse> getExecutionLogs() {
        List<BatchExecutionLogResponse> logs = new ArrayList<>();

        for (BatchJobSummaryResponse summary : getJobSummaries()) {
            if (summary.getLatestExecutionId() == null) {
                continue;
            }

            logs.add(BatchExecutionLogResponse.builder()
                .type("BATCH_JOB")
                .message("Latest execution status is " + summary.getLatestStatus())
                .jobName(summary.getJobName())
                .executionId(summary.getLatestExecutionId())
                .occurredAt(summary.getLatestEndTime() != null ? summary.getLatestEndTime() : summary.getLatestStartTime())
                .build());
        }

        logs.sort(Comparator.comparing(BatchExecutionLogResponse::getOccurredAt,
            Comparator.nullsLast(Comparator.reverseOrder())));
        return logs;
    }
}