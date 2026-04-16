package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.SecondEvaluationMode;
import com.ohgiraffers.team3backendbatch.domain.qualitative.model.QualitativeKeywordRule;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.QualitativeSubmittedEventStore;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class QualitativeEvaluationReader implements ItemReader<QualitativeEvaluationAggregate> {

    private static final Logger log = LoggerFactory.getLogger(QualitativeEvaluationReader.class);
    private static final String DEFAULT_ANALYSIS_VERSION = "squal-v1";

    private final ObjectMapper objectMapper;
    private final QualitativeSubmittedEventStore qualitativeSubmittedEventStore;
    private final Long qualitativeEvaluationId;
    private final String qualitativeEventPayload;
    private boolean consumed;

    /**
     * 정성 평가 분석 Reader 를 생성한다.
     * @param objectMapper JSON 역직렬화 객체
     * @param qualitativeSubmittedEventStore 제출 이벤트 저장소
     * @param qualitativeEvaluationId 정성 평가 ID
     * @param qualitativeEventPayload 정성 평가 이벤트 payload
     */
    public QualitativeEvaluationReader(
        ObjectMapper objectMapper,
        QualitativeSubmittedEventStore qualitativeSubmittedEventStore,
        @Value("#{jobParameters['qualitativeEvaluationId']}") Long qualitativeEvaluationId,
        @Value("#{jobParameters['qualitativeEventPayload']}") String qualitativeEventPayload
    ) {
        this.objectMapper = objectMapper;
        this.qualitativeSubmittedEventStore = qualitativeSubmittedEventStore;
        this.qualitativeEvaluationId = qualitativeEvaluationId;
        this.qualitativeEventPayload = qualitativeEventPayload;
    }

    /**
     * 정성 평가 분석 대상을 한 건 반환한다.
     * @param 없음
     * @return 정성 평가 분석 집계 데이터
     */
    @Override
    public QualitativeEvaluationAggregate read() {
        if (consumed) {
            return null;
        }
        consumed = true;

        QualitativeEvaluationSubmittedEvent event = deserializePayload();
        QualitativeEvaluationAggregate aggregate = new QualitativeEvaluationAggregate(
            event.getQualitativeEvaluationId(),
            event.getEvaluationPeriodId(),
            event.getAlgorithmVersionId(),
            event.getEvaluateeId(),
            event.getEvaluatorId(),
            event.getEvaluationLevel(),
            resolveSecondEvaluationMode(event),
            event.getBaseRawScore(),
            event.getEvalComment(),
            event.getInputMethod(),
            event.getAnalysisVersion() == null || event.getAnalysisVersion().isBlank()
                ? DEFAULT_ANALYSIS_VERSION
                : event.getAnalysisVersion(),
            event.getOccurredAt(),
            toKeywordRules(event)
        );

        log.info(
            "Loaded qualitative evaluation from event payload. evaluationId={}, periodId={}, level={}, keywordRuleCount={}",
            aggregate.getEvaluationId(),
            aggregate.getEvaluationPeriodId(),
            aggregate.getEvaluationLevel(),
            aggregate.getKeywordRules().size()
        );
        return aggregate;
    }

    /**
     * 정성 평가 제출 이벤트 payload 를 역직렬화한다.
     * @param 없음
     * @return 정성 평가 제출 이벤트
     */
    private QualitativeEvaluationSubmittedEvent deserializePayload() {
        QualitativeEvaluationSubmittedEvent cachedEvent = qualitativeSubmittedEventStore.get(qualitativeEvaluationId);
        if (cachedEvent != null) {
            return cachedEvent;
        }

        if (qualitativeEventPayload == null || qualitativeEventPayload.isBlank()) {
            throw new IllegalStateException("qualitative evaluation snapshot is required for qualitative analysis.");
        }

        try {
            return objectMapper.readValue(qualitativeEventPayload, QualitativeEvaluationSubmittedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize qualitative submitted event payload.", exception);
        }
    }

    /**
     * 2차 평가 모드를 enum 값으로 변환한다.
     * @param event 정성 평가 제출 이벤트
     * @return 2차 평가 모드
     */
    private SecondEvaluationMode resolveSecondEvaluationMode(QualitativeEvaluationSubmittedEvent event) {
        if (event.getSecondEvaluationMode() == null || event.getSecondEvaluationMode().isBlank()) {
            return null;
        }
        return SecondEvaluationMode.valueOf(event.getSecondEvaluationMode());
    }

    /**
     * 이벤트 내 키워드 규칙을 배치 도메인 규칙 객체로 변환한다.
     * @param event 정성 평가 제출 이벤트
     * @return 키워드 규칙 목록
     */
    private List<QualitativeKeywordRule> toKeywordRules(QualitativeEvaluationSubmittedEvent event) {
        if (event.getKeywordRules() == null || event.getKeywordRules().isEmpty()) {
            return List.of();
        }
        return event.getKeywordRules().stream()
            .filter(rule -> rule.getKeyword() != null && !rule.getKeyword().isBlank())
            .filter(rule -> rule.getScoreWeight() != null)
            .map(rule -> new QualitativeKeywordRule(
                rule.getDomainKeywordId(),
                rule.getKeyword(),
                rule.getDomainCompetencyCategory(),
                rule.getScoreWeight()
            ))
            .toList();
    }
}
