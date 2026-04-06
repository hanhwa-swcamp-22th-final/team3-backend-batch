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

            responses.add(new BatchJobSummaryResponse(
                jobName,
                latestExecution != null ? latestExecution.getId() : null,
                latestExecution != null ? latestExecution.getStatus().name() : "NEVER_RUN",
                latestExecution != null ? latestExecution.getStartTime() : null,
                latestExecution != null ? latestExecution.getEndTime() : null
            ));
        }

        responses.sort(Comparator.comparing(BatchJobSummaryResponse::jobName));
        return responses;
    }

    public BatchJobExecutionResponse getExecution(Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);

        if (execution == null) {
            throw new IllegalArgumentException("Unknown executionId: " + executionId);
        }

        return new BatchJobExecutionResponse(
            execution.getId(),
            execution.getJobInstance().getJobName(),
            execution.getStatus().name(),
            execution.getStartTime(),
            execution.getEndTime(),
            execution.getJobParameters().getParameters().entrySet().stream()
                .collect(LinkedHashMap::new,
                    (map, entry) -> map.put(entry.getKey(), String.valueOf(entry.getValue().getValue())),
                    LinkedHashMap::putAll)
        );
    }

    public List<BatchExecutionLogResponse> getExecutionLogs() {
        List<BatchExecutionLogResponse> logs = new ArrayList<>();

        for (BatchJobSummaryResponse summary : getJobSummaries()) {
            if (summary.latestExecutionId() == null) {
                continue;
            }

            logs.add(new BatchExecutionLogResponse(
                "BATCH_JOB",
                "Latest execution status is " + summary.latestStatus(),
                summary.jobName(),
                summary.latestExecutionId(),
                summary.latestEndTime() != null ? summary.latestEndTime() : summary.latestStartTime()
            ));
        }

        logs.sort(Comparator.comparing(BatchExecutionLogResponse::occurredAt,
            Comparator.nullsLast(Comparator.reverseOrder())));
        return logs;
    }
}