package com.ohgiraffers.team3backendbatch.batch.job.quantitative.writer;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EquipmentBaselineCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QuantitativeEquipmentResultEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QuantitativeEvaluationCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QuantitativeEvaluationEventPublisher;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuantitativeEvaluationWriter
    implements ItemWriter<QuantitativeEvaluationAggregate>, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(QuantitativeEvaluationWriter.class);

    private final QuantitativeEvaluationEventPublisher quantitativeEvaluationEventPublisher;
    private final Set<Long> publishedEquipmentBaselineIds = new HashSet<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        publishedEquipmentBaselineIds.clear();
    }

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
            publishEquipmentBaselineCalculatedIfNeeded(item, calculatedAt);
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

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        publishedEquipmentBaselineIds.clear();
        return null;
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

    private void publishEquipmentBaselineCalculatedIfNeeded(
        QuantitativeEvaluationAggregate item,
        LocalDateTime calculatedAt
    ) {
        if (item.getEquipmentId() == null || !publishedEquipmentBaselineIds.add(item.getEquipmentId())) {
            return;
        }

        quantitativeEvaluationEventPublisher.publishEquipmentBaselineCalculated(
            EquipmentBaselineCalculatedEvent.builder()
                .equipmentId(item.getEquipmentId())
                .evaluationPeriodId(item.getEvaluationPeriodId())
                .algorithmVersionId(item.getAlgorithmVersionId())
                .periodType(item.getPeriodType() == null ? null : item.getPeriodType().name())
                .equipmentStandardPerformanceRate(item.getTargetUph())
                .equipmentBaselineErrorRate(item.getBaselineError())
                .equipmentEtaAge(item.getEtaAge())
                .equipmentEtaMaint(item.getEtaMaint())
                .equipmentAgeMonths(item.getEquipmentAgeMonths())
                .equipmentIdx(item.getCurrentEquipmentIdx())
                .currentEquipmentGrade(item.getCurrentEquipmentGrade())
                .calculatedAt(calculatedAt)
                .build()
        );
    }

    private record EmployeePeriodKey(
        Long employeeId,
        Long evaluationPeriodId,
        Long algorithmVersionId,
        BatchPeriodType periodType
    ) {
    }
}
