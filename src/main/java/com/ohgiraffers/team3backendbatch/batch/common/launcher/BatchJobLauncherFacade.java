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
            return new BatchJobLaunchResponse(
                execution.getId(),
                jobName,
                execution.getStatus().name(),
                toStringMap(jobParameters),
                LocalDateTime.now()
            );
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
        launch(jobName, new BatchJobLaunchRequest(
            null,
            periodType,
            evaluationPeriodId,
            null,
            Boolean.FALSE,
            triggerSource,
            reason
        ));
    }

    private Map<String, JobParameter<?>> toJobParameterMap(BatchJobLaunchRequest request) {
        Map<String, JobParameter<?>> parameters = new LinkedHashMap<>();
        parameters.put("requestedAt", new JobParameter<>(System.currentTimeMillis(), Long.class));
        parameters.put("requestedBy", new JobParameter<>(request.requestedBy(), String.class));
        parameters.put("reason", new JobParameter<>(request.reason(), String.class));

        if (request.mode() != null) {
            parameters.put("mode", new JobParameter<>(request.mode().name(), String.class));
        }

        if (request.periodType() != null) {
            parameters.put("periodType", new JobParameter<>(request.periodType().name(), String.class));
        }

        if (request.evaluationPeriodId() != null) {
            parameters.put("evaluationPeriodId", new JobParameter<>(request.evaluationPeriodId(), Long.class));
        }

        if (request.employeeId() != null) {
            parameters.put("employeeId", new JobParameter<>(request.employeeId(), Long.class));
        }

        if (request.force() != null) {
            parameters.put("force", new JobParameter<>(request.force().toString(), String.class));
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