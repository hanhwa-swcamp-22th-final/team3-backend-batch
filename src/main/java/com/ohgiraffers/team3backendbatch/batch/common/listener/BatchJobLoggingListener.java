package com.ohgiraffers.team3backendbatch.batch.common.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class BatchJobLoggingListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(BatchJobLoggingListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Starting batch job [{}] with executionId={}",
            jobExecution.getJobInstance().getJobName(),
            jobExecution.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Completed batch job [{}] with executionId={} and status={}",
            jobExecution.getJobInstance().getJobName(),
            jobExecution.getId(),
            jobExecution.getStatus());
    }
}