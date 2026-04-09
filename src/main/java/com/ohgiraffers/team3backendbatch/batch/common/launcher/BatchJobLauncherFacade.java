package com.ohgiraffers.team3backendbatch.batch.common.launcher;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchJobLaunchRequest;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchJobLaunchResponse;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchJobLauncherFacade {
    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobs;

    public BatchJobLaunchResponse launch(String jobName, BatchJobLaunchRequest request) {
        Job job = jobs.get(jobName);
        if (job == null) {
            throw new IllegalArgumentException("Unknown batch job: " + jobName);
        }

        JobParameters jobParameters = new JobParameters(toJobParameterMap(request));

        try {
            JobExecution execution = jobLauncher.run(job, jobParameters);
            return BatchJobLaunchResponse.builder()
                .executionId(execution.getId())
                .jobName(jobName)
                .status(execution.getStatus().name())
                .parameters(toStringMap(jobParameters))
                .launchedAt(LocalDateTime.now())
                .build();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to launch batch job: " + jobName, exception);
        }
    }

    public void launchScheduled(String jobName, String triggerSource) {
        launchScheduled(jobName, null, null, triggerSource, "Scheduled execution");
    }

    public void launchScheduled(
        String jobName,
        BatchPeriodType periodType,
        Long evaluationPeriodId,
        String triggerSource,
        String reason
    ) {
        launch(jobName, BatchJobLaunchRequest.builder()
            .periodType(periodType)
            .evaluationPeriodId(evaluationPeriodId)
            .force(Boolean.FALSE)
            .requestedBy(triggerSource)
            .reason(reason)
            .build());
    }

    private Map<String, JobParameter<?>> toJobParameterMap(BatchJobLaunchRequest request) {
        Map<String, JobParameter<?>> parameters = new LinkedHashMap<>();
        parameters.put("requestedAt", new JobParameter<>(System.currentTimeMillis(), Long.class));
        parameters.put("requestedBy", new JobParameter<>(request.getRequestedBy(), String.class));
        parameters.put("reason", new JobParameter<>(request.getReason(), String.class));

        if (request.getMode() != null) {
            parameters.put("mode", new JobParameter<>(request.getMode().name(), String.class));
        }
        if (request.getPeriodType() != null) {
            parameters.put("periodType", new JobParameter<>(request.getPeriodType().name(), String.class));
        }
        if (request.getEvaluationPeriodId() != null) {
            parameters.put("evaluationPeriodId", new JobParameter<>(request.getEvaluationPeriodId(), Long.class));
        }
        if (request.getEmployeeId() != null) {
            parameters.put("employeeId", new JobParameter<>(request.getEmployeeId(), Long.class));
        }
        if (request.getOrderId() != null) {
            parameters.put("orderId", new JobParameter<>(request.getOrderId(), Long.class));
        }
        if (request.getQualitativeEvaluationId() != null) {
            parameters.put("qualitativeEvaluationId", new JobParameter<>(request.getQualitativeEvaluationId(), Long.class));
        }
        if (request.getQualitativeEventPayload() != null) {
            parameters.put("qualitativeEventPayload", new JobParameter<>(request.getQualitativeEventPayload(), String.class));
        }
        if (request.getForce() != null) {
            parameters.put("force", new JobParameter<>(request.getForce().toString(), String.class));
        }
        return parameters;
    }

    private Map<String, String> toStringMap(JobParameters jobParameters) {
        return jobParameters.getParameters().entrySet().stream()
            .collect(LinkedHashMap::new,
                (map, entry) -> map.put(entry.getKey(), String.valueOf(entry.getValue().getValue())),
                LinkedHashMap::putAll);
    }
}
