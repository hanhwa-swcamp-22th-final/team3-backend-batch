package com.ohgiraffers.team3backendbatch.batch.job.skillscore;

import com.ohgiraffers.team3backendbatch.batch.common.listener.BatchJobLoggingListener;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.model.IntegratedScoreAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.processor.IntegratedScoreProcessor;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.reader.IntegratedScoreReader;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.writer.PerformancePointWriter;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.writer.ScoreWriter;
import com.ohgiraffers.team3backendbatch.batch.job.skillscore.writer.SkillWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 통합 점수 집계 배치 스켈레톤이다.
 *
 * 구현 기준:
 * - MONTH: 정량/정성/KMS 를 결합해서 공식 score, skill, performance_point 를 갱신한다.
 * - WEEK: preview 용 임시 결과를 만들 수 있지만, 공식 current score/skill 은 갱신하지 않는다.
 * - QUARTER/HALF_YEAR/YEAR: 월간 settlement 결과를 집계해서 summary 결과를 만든다.
 *
 * 참고:
 * - 직원 단위 다건 계산이므로 chunk 구조가 적합하다.
 * - 결과 저장 대상이 여러 테이블이기 때문에 composite writer 로 score/skill/performance_point 를 나눠 처리한다.
 */
@Configuration
@RequiredArgsConstructor
public class ScoreAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchJobLoggingListener batchJobLoggingListener;
    private final IntegratedScoreReader integratedScoreReader;
    private final IntegratedScoreProcessor integratedScoreProcessor;
    private final ScoreWriter scoreWriter;
    private final SkillWriter skillWriter;
    private final PerformancePointWriter performancePointWriter;

    @Bean(name = BatchJobNames.SCORE_AGGREGATION_JOB)
    public Job scoreAggregationJob() {
        return new JobBuilder(BatchJobNames.SCORE_AGGREGATION_JOB, jobRepository)
            .listener(batchJobLoggingListener)
            .start(scoreAggregationStep())
            .build();
    }

    @Bean
    public Step scoreAggregationStep() {
        return new StepBuilder("scoreAggregationStep", jobRepository)
            .<IntegratedScoreAggregate, IntegratedScoreAggregate>chunk(100, transactionManager)
            .reader(integratedScoreReader)
            .processor(integratedScoreProcessor)
            .writer(integratedScoreCompositeWriter())
            .build();
    }

    @Bean
    public CompositeItemWriter<IntegratedScoreAggregate> integratedScoreCompositeWriter() {
        CompositeItemWriter<IntegratedScoreAggregate> writer = new CompositeItemWriter<>();
        writer.setDelegates(List.of(scoreWriter, skillWriter, performancePointWriter));
        return writer;
    }
}
