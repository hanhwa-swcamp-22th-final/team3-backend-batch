package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EvaluationWeightConfigSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.EvaluationWeightConfigProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository.EvaluationWeightConfigProjectionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluationWeightConfigSnapshotListenerTest {

    @Mock
    private EvaluationWeightConfigProjectionRepository repository;

    @InjectMocks
    private EvaluationWeightConfigSnapshotListener listener;

    @Test
    @DisplayName("creates projection when snapshot event is received for new config")
    void listenCreatesProjection() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 14, 10, 0);
        EvaluationWeightConfigSnapshotEvent event = EvaluationWeightConfigSnapshotEvent.builder()
            .evaluationWeightConfigId(10L)
            .tierGroup("SA")
            .categoryCode("PROCESS_INNOVATION")
            .weightPercent(50)
            .active(true)
            .deleted(false)
            .occurredAt(occurredAt)
            .build();
        when(repository.findById(10L)).thenReturn(Optional.empty());

        listener.listen(event);

        ArgumentCaptor<EvaluationWeightConfigProjectionEntity> captor =
            ArgumentCaptor.forClass(EvaluationWeightConfigProjectionEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEvaluationWeightConfigId()).isEqualTo(10L);
        assertThat(captor.getValue().getTierGroup()).isEqualTo("SA");
        assertThat(captor.getValue().getCategoryCode()).isEqualTo("PROCESS_INNOVATION");
        assertThat(captor.getValue().getWeightPercent()).isEqualTo(50);
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
        assertThat(captor.getValue().getOccurredAt()).isEqualTo(occurredAt);
    }

    @Test
    @DisplayName("updates existing projection when snapshot event is received for same config")
    void listenUpdatesProjection() {
        EvaluationWeightConfigProjectionEntity existing = EvaluationWeightConfigProjectionEntity.create(
            10L,
            "BC",
            "PRODUCTIVITY",
            60,
            true,
            false,
            LocalDateTime.of(2026, 4, 1, 9, 0),
            LocalDateTime.of(2026, 4, 1, 9, 0)
        );
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 14, 11, 0);
        EvaluationWeightConfigSnapshotEvent event = EvaluationWeightConfigSnapshotEvent.builder()
            .evaluationWeightConfigId(10L)
            .tierGroup("BC")
            .categoryCode("EQUIPMENT_RESPONSE")
            .weightPercent(20)
            .active(true)
            .deleted(false)
            .occurredAt(occurredAt)
            .build();
        when(repository.findById(10L)).thenReturn(Optional.of(existing));

        listener.listen(event);

        ArgumentCaptor<EvaluationWeightConfigProjectionEntity> captor =
            ArgumentCaptor.forClass(EvaluationWeightConfigProjectionEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(captor.getValue().getTierGroup()).isEqualTo("BC");
        assertThat(captor.getValue().getCategoryCode()).isEqualTo("EQUIPMENT_RESPONSE");
        assertThat(captor.getValue().getWeightPercent()).isEqualTo(20);
        assertThat(captor.getValue().getOccurredAt()).isEqualTo(occurredAt);
    }
}
