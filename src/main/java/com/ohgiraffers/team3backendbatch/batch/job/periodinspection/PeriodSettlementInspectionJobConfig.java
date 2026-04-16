package com.ohgiraffers.team3backendbatch.batch.job.periodinspection;

import com.ohgiraffers.team3backendbatch.batch.common.listener.BatchJobLoggingListener;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model.PeriodSettlementInspectionResult;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.model.PeriodSettlementInspectionTarget;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.processor.PeriodSettlementInspectionProcessor;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.reader.PeriodSettlementInspectionReader;
import com.ohgiraffers.team3backendbatch.batch.job.periodinspection.writer.PeriodSettlementInspectionWriter;
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
public class PeriodSettlementInspectionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchJobLoggingListener batchJobLoggingListener;
    private final PeriodSettlementInspectionReader periodSettlementInspectionReader;
    private final PeriodSettlementInspectionProcessor periodSettlementInspectionProcessor;
    private final PeriodSettlementInspectionWriter periodSettlementInspectionWriter;

    /**
     * 상위 기간 정산 점검 배치 Job 을 등록한다.
     * @param 없음
     * @return 상위 기간 정산 점검 Job
     */
    @Bean(name = BatchJobNames.PERIOD_SETTLEMENT_INSPECTION_JOB)
    public Job periodSettlementInspectionJob() {
        return new JobBuilder(BatchJobNames.PERIOD_SETTLEMENT_INSPECTION_JOB, jobRepository)
            .listener(batchJobLoggingListener)
            .start(periodSettlementInspectionStep())
            .build();
    }

    /**
     * 상위 기간 정산 점검 Step 을 등록한다.
     * @param 없음
     * @return 상위 기간 정산 점검 Step
     */
    @Bean
    public Step periodSettlementInspectionStep() {
        return new StepBuilder("periodSettlementInspectionStep", jobRepository)
            .<PeriodSettlementInspectionTarget, PeriodSettlementInspectionResult>chunk(100, transactionManager)
            .reader(periodSettlementInspectionReader)
            .processor(periodSettlementInspectionProcessor)
            .writer(periodSettlementInspectionWriter)
            .build();
    }
}
