package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.QualitativeSubmittedEventStore;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativeAnalysisJobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(QualitativeAnalysisJobCompletionListener.class);

    private final QualitativeSubmittedEventStore qualitativeSubmittedEventStore;

    /**
     * 배치 종료 후 캐시된 정성 평가 제출 이벤트를 정리한다.
     * @param jobExecution 현재 Job 실행 정보
     * @return 반환값 없음
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        Long qualitativeEvaluationId = jobExecution.getJobParameters().getLong("qualitativeEvaluationId");
        if (qualitativeEvaluationId == null) {
            return;
        }
        qualitativeSubmittedEventStore.remove(qualitativeEvaluationId);
        log.debug(
            "Removed qualitative submitted event from store. evaluationId={}, jobStatus={}",
            qualitativeEvaluationId,
            jobExecution.getStatus()
        );
    }
}
