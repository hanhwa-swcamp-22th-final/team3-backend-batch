package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.reader;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.SecondEvaluationMode;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationSubmittedEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class QualitativeEvaluationReaderTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void read_shouldDeserializePayloadIntoAggregate() throws Exception {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 6, 9, 0);
        String payload = objectMapper.writeValueAsString(new QualitativeEvaluationSubmittedEvent(
            1L,
            202604L,
            101L,
            201L,
            301L,
            1L,
            null,
            null,
            "first comment",
            "TEXT",
            "squal-v1",
            "SUBMITTED",
            occurredAt,
            null
        ));

        QualitativeEvaluationReader reader = new QualitativeEvaluationReader(objectMapper, payload);

        QualitativeEvaluationAggregate aggregate = reader.read();
        assertThat(aggregate.getEvaluationId()).isEqualTo(1L);
        assertThat(aggregate.getEvaluationPeriodId()).isEqualTo(202604L);
        assertThat(aggregate.getAlgorithmVersionId()).isEqualTo(101L);
        assertThat(aggregate.getEmployeeId()).isEqualTo(201L);
        assertThat(aggregate.getEvaluatorId()).isEqualTo(301L);
        assertThat(aggregate.getEvaluationLevel()).isEqualTo(1L);
        assertThat(aggregate.getSecondEvaluationMode()).isNull();
        assertThat(aggregate.getBaseRawScore()).isNull();
        assertThat(aggregate.getCommentText()).isEqualTo("first comment");
        assertThat(aggregate.getInputMethod()).isEqualTo("TEXT");
        assertThat(aggregate.getAnalysisVersion()).isEqualTo("squal-v1");
        assertThat(aggregate.getSubmittedAt()).isEqualTo(occurredAt);
        assertThat(aggregate.getKeywordRules()).isEmpty();
        assertThat(reader.read()).isNull();
    }

    @Test
    void read_shouldResolveSecondEvaluationModeFromPayload() throws Exception {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 6, 10, 0);
        String payload = objectMapper.writeValueAsString(new QualitativeEvaluationSubmittedEvent(
            11L,
            202604L,
            101L,
            201L,
            301L,
            2L,
            SecondEvaluationMode.KEEP_FIRST_SCORE.name(),
            BigDecimal.valueOf(1.3200),
            null,
            "TEXT",
            "",
            "SUBMITTED",
            occurredAt,
            null
        ));

        QualitativeEvaluationReader reader = new QualitativeEvaluationReader(objectMapper, payload);

        QualitativeEvaluationAggregate aggregate = reader.read();
        assertThat(aggregate.getEvaluationId()).isEqualTo(11L);
        assertThat(aggregate.getSecondEvaluationMode()).isEqualTo(SecondEvaluationMode.KEEP_FIRST_SCORE);
        assertThat(aggregate.getBaseRawScore()).isEqualByComparingTo("1.3200");
        assertThat(aggregate.getAnalysisVersion()).isEqualTo("squal-v1");
        assertThat(reader.read()).isNull();
    }
}
