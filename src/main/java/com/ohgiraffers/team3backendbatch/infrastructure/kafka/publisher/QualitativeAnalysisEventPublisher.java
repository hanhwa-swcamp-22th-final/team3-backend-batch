package com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.QualitativeKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativeAnalysisEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(QualitativeAnalysisEventPublisher.class);

    private final KafkaTemplate<String, QualitativeEvaluationAnalyzedEvent> qualitativeAnalyzedKafkaTemplate;

    public void publishAnalyzed(QualitativeEvaluationAnalyzedEvent event) {
        qualitativeAnalyzedKafkaTemplate.send(
            QualitativeKafkaTopics.QUALITATIVE_EVALUATION_ANALYZED,
            String.valueOf(event.getQualitativeEvaluationId()),
            event
        );
        log.info(
            "Published qualitative analyzed event. evaluationId={}, status={}, rawScore={}, sQual={}",
            event.getQualitativeEvaluationId(),
            event.getAnalysisStatus(),
            event.getRawScore(),
            event.getSQual()
        );
    }
}
