package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization;

import com.ohgiraffers.team3backendbatch.batch.common.listener.BatchJobLoggingListener;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationTarget;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.processor.QualitativeNormalizationProcessor;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.reader.QualitativeNormalizationReader;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.writer.QualitativeNormalizationWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class QualitativeNormalizationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchJobLoggingListener batchJobLoggingListener;
    private final QualitativeNormalizationReader qualitativeNormalizationReader;
    private final QualitativeNormalizationProcessor qualitativeNormalizationProcessor;
    private final QualitativeNormalizationWriter qualitativeNormalizationWriter;

    @Bean(name = BatchJobNames.QUALITATIVE_NORMALIZATION_JOB)
    public Job qualitativeNormalizationJob() {
        return new JobBuilder(BatchJobNames.QUALITATIVE_NORMALIZATION_JOB, jobRepository)
            .listener(batchJobLoggingListener)
            .start(qualitativeNormalizationStep())
            .build();
    }

    @Bean
    public Step qualitativeNormalizationStep() {
        return new StepBuilder("qualitativeNormalizationStep", jobRepository)
            .<QualitativeNormalizationTarget, QualitativeNormalizationResult>chunk(100, transactionManager)
            .reader(qualitativeNormalizationReader)
            .processor(qualitativeNormalizationProcessor)
            .writer(qualitativeNormalizationWriter)
            .build();
    }
}