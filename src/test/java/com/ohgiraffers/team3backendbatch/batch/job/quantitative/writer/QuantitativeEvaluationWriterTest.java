package com.ohgiraffers.team3backendbatch.batch.job.quantitative.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.QuantitativeEvaluationEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.QuantitativeEvaluationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class QuantitativeEvaluationWriterTest {

    @Mock
    private QuantitativeEvaluationRepository quantitativeEvaluationRepository;

    @Mock
    private IdGenerator idGenerator;

    @Test
    void write_shouldCreateNewEntityWhenMissing() {
        QuantitativeEvaluationAggregate item = QuantitativeEvaluationAggregate.builder()
            .employeeId(10L)
            .evaluationPeriodId(202601L)
            .equipmentId(3L)
            .periodType(BatchPeriodType.MONTH)
            .uphScore(BigDecimal.valueOf(88.5))
            .yieldScore(BigDecimal.valueOf(92.0))
            .leadTimeScore(BigDecimal.valueOf(80.0))
            .actualError(BigDecimal.valueOf(3.5))
            .materialShielding(BigDecimal.ONE)
            .sQuant(BigDecimal.valueOf(87.2))
            .tScore(BigDecimal.valueOf(87.2))
            .status("SETTLED")
            .build();

        when(quantitativeEvaluationRepository.findByEmployeeIdAndEvaluationPeriodIdAndEquipmentId(10L, 202601L, 3L))
            .thenReturn(Optional.empty());
        when(idGenerator.generate()).thenReturn(999L);

        QuantitativeEvaluationWriter writer = new QuantitativeEvaluationWriter(
            quantitativeEvaluationRepository,
            idGenerator
        );
        writer.write(new Chunk<>(List.of(item)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QuantitativeEvaluationEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(quantitativeEvaluationRepository).saveAll(captor.capture());
        QuantitativeEvaluationEntity saved = captor.getValue().get(0);
        assertThat(saved.getQuantitativeEvaluationId()).isEqualTo(999L);
        assertThat(saved.getEmployeeId()).isEqualTo(10L);
        assertThat(saved.getEvaluationPeriodId()).isEqualTo(202601L);
        assertThat(saved.getEquipmentId()).isEqualTo(3L);
        assertThat(saved.getMaterialShielding()).isEqualTo(1);
        assertThat(saved.getSQuant()).isEqualByComparingTo("87.2");
        assertThat(saved.getTScore()).isEqualByComparingTo("87.2");
        assertThat(saved.getStatus()).isEqualTo("SETTLED");
    }

    @Test
    void write_shouldUpdateExistingEntity() {
        QuantitativeEvaluationAggregate item = QuantitativeEvaluationAggregate.builder()
            .employeeId(10L)
            .evaluationPeriodId(202601L)
            .equipmentId(3L)
            .periodType(BatchPeriodType.WEEK)
            .uphScore(BigDecimal.valueOf(70.0))
            .yieldScore(BigDecimal.valueOf(82.0))
            .leadTimeScore(BigDecimal.valueOf(77.0))
            .actualError(BigDecimal.valueOf(4.0))
            .materialShielding(BigDecimal.ZERO)
            .sQuant(BigDecimal.valueOf(75.0))
            .tScore(null)
            .status("PREVIEW")
            .build();

        QuantitativeEvaluationEntity existing = QuantitativeEvaluationEntity.create(500L, 10L, 202601L, 3L);
        when(quantitativeEvaluationRepository.findByEmployeeIdAndEvaluationPeriodIdAndEquipmentId(10L, 202601L, 3L))
            .thenReturn(Optional.of(existing));

        QuantitativeEvaluationWriter writer = new QuantitativeEvaluationWriter(
            quantitativeEvaluationRepository,
            idGenerator
        );
        writer.write(new Chunk<>(List.of(item)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QuantitativeEvaluationEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(quantitativeEvaluationRepository).saveAll(captor.capture());
        QuantitativeEvaluationEntity saved = captor.getValue().get(0);
        assertThat(saved.getQuantitativeEvaluationId()).isEqualTo(500L);
        assertThat(saved.getMaterialShielding()).isEqualTo(0);
        assertThat(saved.getSQuant()).isEqualByComparingTo("75.0");
        assertThat(saved.getTScore()).isNull();
        assertThat(saved.getStatus()).isEqualTo("PREVIEW");
    }
}