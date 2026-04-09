package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchJobLaunchRequest;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.api.command.dto.ManualJobLaunchMode;
import com.ohgiraffers.team3backendbatch.batch.common.launcher.BatchJobLauncherFacade;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.QualitativeKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativeEvaluationSubmittedListener {
    private static final Logger log = LoggerFactory.getLogger(QualitativeEvaluationSubmittedListener.class);

    private final BatchJobLauncherFacade batchJobLauncherFacade;

    @KafkaListener(
        topics = QualitativeKafkaTopics.QUALITATIVE_EVALUATION_SUBMITTED,
        containerFactory = "qualitativeSubmittedKafkaListenerContainerFactory"
    )
    public void listen(QualitativeEvaluationSubmittedEvent event) {
        log.info(
            "Received qualitative submitted event. evaluationId={}, evaluateeId={}, periodId={}, level={}",
            event.getQualitativeEvaluationId(),
            event.getEvaluateeId(),
            event.getEvaluationPeriodId(),
            event.getEvaluationLevel()
        );

        batchJobLauncherFacade.launch(
            BatchJobNames.QUALITATIVE_ANALYSIS_JOB,
            BatchJobLaunchRequest.builder()
                .mode(ManualJobLaunchMode.EMPLOYEE)
                .periodType(BatchPeriodType.MONTH)
                .evaluationPeriodId(event.getEvaluationPeriodId())
                .qualitativeEvaluationId(event.getQualitativeEvaluationId())
                .force(Boolean.TRUE)
                .requestedBy("hr-kafka")
                .reason("Qualitative evaluation submitted event")
                .build()
        );
    }
}