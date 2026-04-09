package com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationNormalizedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.QualitativeKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QualitativeNormalizationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(QualitativeNormalizationEventPublisher.class);

    private final KafkaTemplate<String, QualitativeEvaluationNormalizedEvent> qualitativeNormalizedKafkaTemplate;

    public void publishNormalized(QualitativeEvaluationNormalizedEvent event) {
        qualitativeNormalizedKafkaTemplate.send(
            QualitativeKafkaTopics.QUALITATIVE_EVALUATION_NORMALIZED,
            String.valueOf(event.getQualitativeEvaluationId()),
            event
        );
        log.info(
            "Published qualitative normalized event. evaluationId={}, rawScore={}, sQual={}, grade={}",
            event.getQualitativeEvaluationId(),
            event.getRawScore(),
            event.getSQual(),
            event.getGrade()
        );
    }
}
