package com.ohgiraffers.team3backendbatch.batch.job.promotion;

import com.ohgiraffers.team3backendbatch.batch.common.listener.BatchJobLoggingListener;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.batch.job.promotion.model.PromotionCandidateSnapshot;
import com.ohgiraffers.team3backendbatch.batch.job.promotion.processor.PromotionCandidateProcessor;
import com.ohgiraffers.team3backendbatch.batch.job.promotion.reader.PromotionCandidateReader;
import com.ohgiraffers.team3backendbatch.batch.job.promotion.writer.PromotionHistoryWriter;
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
 * 승진 후보 판정 배치 스켈레톤이다.
 *
 * 구현 기준:
 * - 주간 preview 값은 승진 판정에 사용하지 않는다.
 * - 월간 settlement 로 누적된 공식 tier 점수 또는 상위 기간 summary 결과를 기준으로 판정한다.
 *
 * 참고:
 * - 직원 단위 후보 판정/기록은 chunk 구조가 적합하다.
 */
@Configuration
@RequiredArgsConstructor
public class PromotionCandidateJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchJobLoggingListener batchJobLoggingListener;
    private final PromotionCandidateReader promotionCandidateReader;
    private final PromotionCandidateProcessor promotionCandidateProcessor;
    private final PromotionHistoryWriter promotionHistoryWriter;

    @Bean(name = BatchJobNames.PROMOTION_CANDIDATE_JOB)
    public Job promotionCandidateJob() {
        return new JobBuilder(BatchJobNames.PROMOTION_CANDIDATE_JOB, jobRepository)
            .listener(batchJobLoggingListener)
            .start(promotionCandidateStep())
            .build();
    }

    @Bean
    public Step promotionCandidateStep() {
        return new StepBuilder("promotionCandidateStep", jobRepository)
            .<PromotionCandidateSnapshot, PromotionCandidateSnapshot>chunk(100, transactionManager)
            .reader(promotionCandidateReader)
            .processor(promotionCandidateProcessor)
            .writer(promotionHistoryWriter)
            .build();
    }
}