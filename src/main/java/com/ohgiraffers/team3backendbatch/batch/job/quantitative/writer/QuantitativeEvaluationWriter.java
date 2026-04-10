package com.ohgiraffers.team3backendbatch.batch.job.quantitative.writer;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QuantitativeEquipmentResultEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QuantitativeEvaluationCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QuantitativeEvaluationEventPublisher;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuantitativeEvaluationWriter implements ItemWriter<QuantitativeEvaluationAggregate> {

    private static final Logger log = LoggerFactory.getLogger(QuantitativeEvaluationWriter.class);

    private final QuantitativeEvaluationEventPublisher quantitativeEvaluationEventPublisher;

    @Override
    public void write(Chunk<? extends QuantitativeEvaluationAggregate> chunk) {
        LocalDateTime calculatedAt = LocalDateTime.now();
        Map<EmployeePeriodKey, List<QuantitativeEvaluationAggregate>> groupedByEmployee = new LinkedHashMap<>();

        for (QuantitativeEvaluationAggregate item : chunk.getItems()) {
            EmployeePeriodKey key = new EmployeePeriodKey(
                item.getEmployeeId(),
                item.getEvaluationPeriodId(),
                item.getAlgorithmVersionId(),
                item.getPeriodType()
            );
            groupedByEmployee.computeIfAbsent(key, ignored -> new ArrayList<>()).add(item);
        }

        for (Map.Entry<EmployeePeriodKey, List<QuantitativeEvaluationAggregate>> entry : groupedByEmployee.entrySet()) {
            QuantitativeEvaluationCalculatedEvent event = QuantitativeEvaluationCalculatedEvent.builder()
                .employeeId(entry.getKey().employeeId())
                .evaluationPeriodId(entry.getKey().evaluationPeriodId())
                .algorithmVersionId(entry.getKey().algorithmVersionId())
                .periodType(entry.getKey().periodType() == null ? null : entry.getKey().periodType().name())
                .calculatedAt(calculatedAt)
                .equipmentResults(entry.getValue().stream().map(this::toEquipmentResult).toList())
                .build();
            quantitativeEvaluationEventPublisher.publishCalculated(event);
        }

        log.info(
            "Published quantitative calculated events. employeeGroupCount={}, itemCount={}",
            groupedByEmployee.size(),
            chunk.getItems().size()
        );
    }

    private QuantitativeEquipmentResultEvent toEquipmentResult(QuantitativeEvaluationAggregate item) {
        return QuantitativeEquipmentResultEvent.builder()
            .equipmentId(item.getEquipmentId())
            .uphScore(item.getUphScore())
            .yieldScore(item.getYieldScore())
            .leadTimeScore(item.getLeadTimeScore())
            .actualError(item.getActualError())
            .sQuant(item.getSQuant())
            .tScore(item.getTScore())
            .materialShielding(item.getMaterialShielding() == null ? null : item.getMaterialShielding().intValue())
            .status(item.getStatus())
            .build();
    }

    private record EmployeePeriodKey(
        Long employeeId,
        Long evaluationPeriodId,
        Long algorithmVersionId,
        BatchPeriodType periodType
    ) {
    }
}
