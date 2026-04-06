package com.ohgiraffers.team3backendbatch.batch.job.qualitative;

import com.ohgiraffers.team3backendbatch.batch.common.listener.BatchJobLoggingListener;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.processor.QualitativeEvaluationProcessor;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.reader.QualitativeEvaluationReader;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.writer.QualitativeAnalysisWriter;
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
 * 정성 평가 원문을 NLP 기반 정성 점수로 변환하는 배치 스켈레톤이다.
 *
 * 책임:
 * - HR 서비스가 저장한 평가 코멘트 원문을 읽는다.
 * - Google NL API 기반 분석 결과와 도메인 키워드 사전을 결합해 S_qual 을 계산한다.
 * - 계산 결과를 qualitative analysis 결과 저장소나 qualitative_evaluation 최종 점수 컬럼에 반영한다.
 *
 * 참고:
 * - 이 잡은 월간 정산 전에 선행 실행되거나, 코멘트 제출 직후 비동기 재계산 용도로도 사용할 수 있다.
 * - HR 는 원문 작성만 담당하고, 점수 산정 책임은 Batch 가 가진다.
 */
@Configuration
@RequiredArgsConstructor
public class QualitativeAnalysisJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchJobLoggingListener batchJobLoggingListener;
    private final QualitativeEvaluationReader qualitativeEvaluationReader;
    private final QualitativeEvaluationProcessor qualitativeEvaluationProcessor;
    private final QualitativeAnalysisWriter qualitativeAnalysisWriter;

    @Bean(name = BatchJobNames.QUALITATIVE_ANALYSIS_JOB)
    public Job qualitativeAnalysisJob() {
        return new JobBuilder(BatchJobNames.QUALITATIVE_ANALYSIS_JOB, jobRepository)
            .listener(batchJobLoggingListener)
            .start(qualitativeAnalysisStep())
            .build();
    }

    @Bean
    public Step qualitativeAnalysisStep() {
        return new StepBuilder("qualitativeAnalysisStep", jobRepository)
            .<QualitativeEvaluationAggregate, QualitativeAnalysisResult>chunk(50, transactionManager)
            .reader(qualitativeEvaluationReader)
            .processor(qualitativeEvaluationProcessor)
            .writer(qualitativeAnalysisWriter)
            .build();
    }
}