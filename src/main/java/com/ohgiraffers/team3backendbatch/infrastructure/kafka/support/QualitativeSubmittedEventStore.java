package com.ohgiraffers.team3backendbatch.infrastructure.kafka.support;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class QualitativeSubmittedEventStore {

    private final Map<Long, QualitativeEvaluationSubmittedEvent> store = new ConcurrentHashMap<>();

    public void put(QualitativeEvaluationSubmittedEvent event) {
        if (event == null || event.getQualitativeEvaluationId() == null) {
            return;
        }
        store.put(event.getQualitativeEvaluationId(), event);
    }

    public QualitativeEvaluationSubmittedEvent get(Long qualitativeEvaluationId) {
        if (qualitativeEvaluationId == null) {
            return null;
        }
        return store.get(qualitativeEvaluationId);
    }

    public void remove(Long qualitativeEvaluationId) {
        if (qualitativeEvaluationId == null) {
            return;
        }
        store.remove(qualitativeEvaluationId);
    }
}
