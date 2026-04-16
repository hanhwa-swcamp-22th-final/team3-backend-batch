package com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary;

import com.ohgiraffers.team3backendbatch.batch.common.listener.BatchJobLoggingListener;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.model.QualitativePeriodSummaryResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.model.QualitativePeriodSummaryTarget;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.processor.QualitativePeriodSummaryProcessor;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.reader.QualitativePeriodSummaryReader;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.writer.QualitativePeriodSummaryWriter;
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
public class QualitativePeriodSummaryJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchJobLoggingListener batchJobLoggingListener;
    private final QualitativePeriodSummaryReader qualitativePeriodSummaryReader;
    private final QualitativePeriodSummaryProcessor qualitativePeriodSummaryProcessor;
    private final QualitativePeriodSummaryWriter qualitativePeriodSummaryWriter;

    /**
     * 정성 기간 요약 배치 Job 을 생성한다.
     * @param 없음
     * @return 정성 기간 요약 Job 객체
     */
    @Bean(name = BatchJobNames.QUALITATIVE_PERIOD_SUMMARY_JOB)
    public Job qualitativePeriodSummaryJob() {
        return new JobBuilder(BatchJobNames.QUALITATIVE_PERIOD_SUMMARY_JOB, jobRepository)
            .listener(batchJobLoggingListener)
            .start(qualitativePeriodSummaryStep())
            .build();
    }

    /**
     * 정성 기간 요약 Step 을 생성한다.
     * @param 없음
     * @return 정성 기간 요약 Step 객체
     */
    @Bean
    public Step qualitativePeriodSummaryStep() {
        return new StepBuilder("qualitativePeriodSummaryStep", jobRepository)
            .<QualitativePeriodSummaryTarget, QualitativePeriodSummaryResult>chunk(100, transactionManager)
            .reader(qualitativePeriodSummaryReader)
            .processor(qualitativePeriodSummaryProcessor)
            .writer(qualitativePeriodSummaryWriter)
            .build();
    }
}
