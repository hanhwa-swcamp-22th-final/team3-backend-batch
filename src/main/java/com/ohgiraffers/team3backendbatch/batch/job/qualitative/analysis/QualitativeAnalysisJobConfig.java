package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis;

import com.ohgiraffers.team3backendbatch.batch.common.listener.BatchJobLoggingListener;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.processor.QualitativeEvaluationProcessor;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.reader.QualitativeEvaluationReader;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.writer.QualitativeAnalysisWriter;
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
 * 정성 평가 원문을 NLP 기반 정성 점수로 변환하는 배치 설정이다.
 */
@Configuration
@RequiredArgsConstructor
public class QualitativeAnalysisJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchJobLoggingListener batchJobLoggingListener;
    private final QualitativeAnalysisJobCompletionListener qualitativeAnalysisJobCompletionListener;
    private final QualitativeEvaluationReader qualitativeEvaluationReader;
    private final QualitativeEvaluationProcessor qualitativeEvaluationProcessor;
    private final QualitativeAnalysisWriter qualitativeAnalysisWriter;

    /**
     * 정성 평가 분석 배치 Job 을 등록한다.
     * @param 없음
     * @return 정성 평가 분석 Job
     */
    @Bean(name = BatchJobNames.QUALITATIVE_ANALYSIS_JOB)
    public Job qualitativeAnalysisJob() {
        return new JobBuilder(BatchJobNames.QUALITATIVE_ANALYSIS_JOB, jobRepository)
            .listener(batchJobLoggingListener)
            .listener(qualitativeAnalysisJobCompletionListener)
            .start(qualitativeAnalysisStep())
            .build();
    }

    /**
     * 정성 평가 분석 Step 을 등록한다.
     * @param 없음
     * @return 정성 평가 분석 Step
     */
    @Bean
    public Step qualitativeAnalysisStep() {
        return new StepBuilder("qualitativeAnalysisStep", jobRepository)
            // 정성 평가는 외부 NLP 호출 비용을 고려해 작은 chunk로 시작한다.
            .<QualitativeEvaluationAggregate, QualitativeAnalysisResult>chunk(10, transactionManager)
            .reader(qualitativeEvaluationReader)
            .processor(qualitativeEvaluationProcessor)
            .writer(qualitativeAnalysisWriter)
            // TODO 필요 시 faultTolerant / retry / skip 정책 추가
            .build();
    }
}
