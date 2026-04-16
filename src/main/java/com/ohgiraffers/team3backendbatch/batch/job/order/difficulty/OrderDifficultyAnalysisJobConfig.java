package com.ohgiraffers.team3backendbatch.batch.job.order.difficulty;

import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultyResult;
import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultySource;
import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.processor.OrderDifficultyProcessor;
import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.reader.OrderDifficultyReader;
import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.writer.OrderDifficultyAnalysisWriter;
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
public class OrderDifficultyAnalysisJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderDifficultyReader orderDifficultyReader;
    private final OrderDifficultyProcessor orderDifficultyProcessor;
    private final OrderDifficultyAnalysisWriter orderDifficultyAnalysisWriter;

    /**
     * 주문 난이도 분석 배치 Job 을 등록한다.
     * @param 없음
     * @return 주문 난이도 분석 Job
     */
    @Bean
    public Job orderDifficultyAnalysisJob() {
        return new JobBuilder("orderDifficultyAnalysisJob", jobRepository)
            .start(orderDifficultyAnalysisStep())
            .build();
    }

    /**
     * 주문 난이도 분석 Step 을 등록한다.
     * @param 없음
     * @return 주문 난이도 분석 Step
     */
    @Bean
    public Step orderDifficultyAnalysisStep() {
        return new StepBuilder("orderDifficultyAnalysisStep", jobRepository)
            .<OrderDifficultySource, OrderDifficultyResult>chunk(1, transactionManager)
            .reader(orderDifficultyReader)
            .processor(orderDifficultyProcessor)
            .writer(orderDifficultyAnalysisWriter)
            .build();
    }
}
