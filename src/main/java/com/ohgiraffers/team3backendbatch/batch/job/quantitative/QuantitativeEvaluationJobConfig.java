package com.ohgiraffers.team3backendbatch.batch.job.quantitative;

import com.ohgiraffers.team3backendbatch.batch.common.listener.BatchJobLoggingListener;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.processor.QuantitativeEvaluationProcessor;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.reader.QuantitativeSourceReader;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.tasklet.AntiGamingFlagRefreshTasklet;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.writer.QuantitativeEvaluationWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 정량 평가 배치 스켈레톤이다.
 *
 * 구현 기준:
 * - WEEK: raw `mes_*` 를 읽어 주간 preview 정량 점수를 계산한다.
 * - MONTH: raw `mes_*` 를 읽어 월간 공식 settlement 정량 점수를 계산한다.
 * - QUARTER/HALF_YEAR/YEAR: 이 잡에서 raw 원천을 다시 계산하지 않는다.
 *   상위 기간 summary 는 월간 settlement 결과를 집계하는 별도 흐름에서 처리한다.
 *
 * 참고:
 * - 정량 계산은 대량 원천 데이터를 다루므로 tasklet 보다 chunk 구조가 적합하다.
 * - 실제 구현 시 reader -> processor -> writer 각각에 periodType 분기와 멱등 upsert 정책을 넣는다.
 */
@Configuration
@RequiredArgsConstructor
public class QuantitativeEvaluationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchJobLoggingListener batchJobLoggingListener;
    private final AntiGamingFlagRefreshTasklet antiGamingFlagRefreshTasklet;
    private final QuantitativeSourceReader quantitativeSourceReader;
    private final QuantitativeEvaluationProcessor quantitativeEvaluationProcessor;
    private final QuantitativeEvaluationWriter quantitativeEvaluationWriter;

    /**
     * 정량 평가 배치 Job 을 등록한다.
     * @param 없음
     * @return 정량 평가 Job
     */
    @Bean(name = BatchJobNames.QUANTITATIVE_EVALUATION_JOB)
    public Job quantitativeEvaluationJob() {
        return new JobBuilder(BatchJobNames.QUANTITATIVE_EVALUATION_JOB, jobRepository)
            .listener(batchJobLoggingListener)
            .start(antiGamingFlagRefreshStep())
            .next(quantitativeEvaluationStep())
            .build();
    }

    /**
     * anti-gaming flag 갱신 Step 을 등록한다.
     * @param 없음
     * @return anti-gaming flag 갱신 Step
     */
    @Bean
    public Step antiGamingFlagRefreshStep() {
        return new StepBuilder("antiGamingFlagRefreshStep", jobRepository)
            .tasklet(antiGamingFlagRefreshTasklet, transactionManager)
            .build();
    }

    /**
     * 정량 평가 Step 을 등록한다.
     * @param 없음
     * @return 정량 평가 Step
     */
    @Bean
    public Step quantitativeEvaluationStep() {
        return new StepBuilder("quantitativeEvaluationStep", jobRepository)
            .<QuantitativeEvaluationAggregate, QuantitativeEvaluationAggregate>chunk(100, transactionManager)
            .reader(quantitativeSourceReader)
            .processor(quantitativeEvaluationProcessor)
            .writer(quantitativeEvaluationWriter)
            .listener(quantitativeEvaluationWriter)
            .build();
    }
}
