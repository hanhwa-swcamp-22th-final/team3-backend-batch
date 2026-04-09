package com.ohgiraffers.team3backendbatch.batch.job.quantitative.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QuantitativeEvaluationCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QuantitativeEvaluationEventPublisher;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class QuantitativeEvaluationWriterTest {

    @Mock
    private QuantitativeEvaluationEventPublisher quantitativeEvaluationEventPublisher;

    @Test
    void write_shouldPublishGroupedEventForSingleEmployee() {
        QuantitativeEvaluationAggregate item = QuantitativeEvaluationAggregate.builder()
            .employeeId(10L)
            .evaluationPeriodId(202601L)
            .equipmentId(3L)
            .periodType(BatchPeriodType.MONTH)
            .algorithmVersionId(901L)
            .uphScore(BigDecimal.valueOf(88.5))
            .yieldScore(BigDecimal.valueOf(92.0))
            .leadTimeScore(BigDecimal.valueOf(80.0))
            .actualError(BigDecimal.valueOf(3.5))
            .materialShielding(BigDecimal.ONE)
            .sQuant(BigDecimal.valueOf(87.2))
            .tScore(BigDecimal.valueOf(87.2))
            .status("SETTLED")
            .build();

        QuantitativeEvaluationWriter writer = new QuantitativeEvaluationWriter(quantitativeEvaluationEventPublisher);
        writer.write(new Chunk<>(List.of(item)));

        ArgumentCaptor<QuantitativeEvaluationCalculatedEvent> captor =
            ArgumentCaptor.forClass(QuantitativeEvaluationCalculatedEvent.class);
        verify(quantitativeEvaluationEventPublisher).publishCalculated(captor.capture());
        QuantitativeEvaluationCalculatedEvent event = captor.getValue();
        assertThat(event.getEmployeeId()).isEqualTo(10L);
        assertThat(event.getEvaluationPeriodId()).isEqualTo(202601L);
        assertThat(event.getAlgorithmVersionId()).isEqualTo(901L);
        assertThat(event.getPeriodType()).isEqualTo("MONTH");
        assertThat(event.getEquipmentResults()).hasSize(1);
        assertThat(event.getEquipmentResults().get(0).getEquipmentId()).isEqualTo(3L);
        assertThat(event.getEquipmentResults().get(0).getMaterialShielding()).isEqualTo(1);
        assertThat(event.getEquipmentResults().get(0).getSQuant()).isEqualByComparingTo("87.2");
        assertThat(event.getEquipmentResults().get(0).getTScore()).isEqualByComparingTo("87.2");
        assertThat(event.getEquipmentResults().get(0).getStatus()).isEqualTo("SETTLED");
    }

    @Test
    void write_shouldGroupMultipleEquipmentsForSameEmployee() {
        QuantitativeEvaluationAggregate first = QuantitativeEvaluationAggregate.builder()
            .employeeId(10L)
            .evaluationPeriodId(202601L)
            .equipmentId(3L)
            .periodType(BatchPeriodType.MONTH)
            .algorithmVersionId(901L)
            .uphScore(BigDecimal.valueOf(70.0))
            .yieldScore(BigDecimal.valueOf(82.0))
            .leadTimeScore(BigDecimal.valueOf(77.0))
            .actualError(BigDecimal.valueOf(4.0))
            .materialShielding(BigDecimal.ZERO)
            .sQuant(BigDecimal.valueOf(75.0))
            .tScore(null)
            .status("PREVIEW")
            .build();
        QuantitativeEvaluationAggregate second = QuantitativeEvaluationAggregate.builder()
            .employeeId(10L)
            .evaluationPeriodId(202601L)
            .equipmentId(7L)
            .periodType(BatchPeriodType.MONTH)
            .algorithmVersionId(901L)
            .uphScore(BigDecimal.valueOf(90.0))
            .yieldScore(BigDecimal.valueOf(91.0))
            .leadTimeScore(BigDecimal.valueOf(88.0))
            .actualError(BigDecimal.valueOf(2.0))
            .materialShielding(BigDecimal.ONE)
            .sQuant(BigDecimal.valueOf(92.5))
            .tScore(BigDecimal.valueOf(89.0))
            .status("SETTLED")
            .build();

        QuantitativeEvaluationWriter writer = new QuantitativeEvaluationWriter(quantitativeEvaluationEventPublisher);
        writer.write(new Chunk<>(List.of(first, second)));

        ArgumentCaptor<QuantitativeEvaluationCalculatedEvent> captor =
            ArgumentCaptor.forClass(QuantitativeEvaluationCalculatedEvent.class);
        verify(quantitativeEvaluationEventPublisher).publishCalculated(captor.capture());
        QuantitativeEvaluationCalculatedEvent event = captor.getValue();
        assertThat(event.getEquipmentResults()).hasSize(2);
        assertThat(event.getEquipmentResults())
            .extracting(result -> result.getEquipmentId())
            .containsExactly(3L, 7L);
    }
}
