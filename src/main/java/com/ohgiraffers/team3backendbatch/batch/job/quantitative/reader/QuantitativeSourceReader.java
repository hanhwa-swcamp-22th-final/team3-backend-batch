package com.ohgiraffers.team3backendbatch.batch.job.quantitative.reader;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationSourceRow;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper.QuantitativeEvaluationQueryMapper;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class QuantitativeSourceReader implements ItemReader<QuantitativeEvaluationAggregate> {

    private static final Logger log = LoggerFactory.getLogger(QuantitativeSourceReader.class);

    private final QuantitativeEvaluationQueryMapper quantitativeEvaluationQueryMapper;
    private final Long evaluationPeriodId;
    private final Long employeeId;
    private final boolean force;
    private final BatchPeriodType periodType;

    private Iterator<QuantitativeEvaluationAggregate> iterator;

    public QuantitativeSourceReader(
        QuantitativeEvaluationQueryMapper quantitativeEvaluationQueryMapper,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId,
        @Value("#{jobParameters['employeeId']}") Long employeeId,
        @Value("#{jobParameters['force']}") String force,
        @Value("#{jobParameters['periodType']}") String periodType
    ) {
        this.quantitativeEvaluationQueryMapper = quantitativeEvaluationQueryMapper;
        this.evaluationPeriodId = evaluationPeriodId;
        this.employeeId = employeeId;
        this.force = Boolean.parseBoolean(force);
        this.periodType = parsePeriodType(periodType);
    }

    @Override
    public QuantitativeEvaluationAggregate read() {
        if (iterator == null) {
            if (evaluationPeriodId == null) {
                log.warn("No evaluationPeriodId job parameter provided for quantitative evaluation job.");
                return null;
            }

            List<QuantitativeEvaluationAggregate> items = quantitativeEvaluationQueryMapper
                .findQuantitativeSourcesForEvaluation(evaluationPeriodId, employeeId, force)
                .stream()
                .map(this::toAggregate)
                .toList();

            iterator = items.iterator();
            log.info(
                "Loaded quantitative source rows. evaluationPeriodId={}, employeeId={}, periodType={}, force={}, count={}",
                evaluationPeriodId,
                employeeId,
                periodType,
                force,
                items.size()
            );
        }

        if (!iterator.hasNext()) {
            return null;
        }
        return iterator.next();
    }

    private QuantitativeEvaluationAggregate toAggregate(QuantitativeEvaluationSourceRow row) {
        return QuantitativeEvaluationAggregate.builder()
            .quantitativeEvaluationId(row.getQuantitativeEvaluationId())
            .employeeId(row.getEmployeeId())
            .evaluationPeriodId(row.getEvaluationPeriodId())
            .equipmentId(row.getEquipmentId())
            .periodType(periodType)
            .algorithmVersionId(row.getAlgorithmVersionId())
            .evaluationPeriodEndDate(row.getEvaluationPeriodEndDate())
            .totalInputQty(row.getTotalInputQty())
            .totalGoodQty(row.getTotalGoodQty())
            .totalDefectQty(row.getTotalDefectQty())
            .averageLeadTimeSec(row.getAverageLeadTimeSec())
            .downtimeMinutes(row.getDowntimeMinutes())
            .maintenanceMinutes(row.getMaintenanceMinutes())
            .targetUph(row.getTargetUph())
            .targetYieldRate(row.getTargetYieldRate())
            .targetLeadTimeSec(row.getTargetLeadTimeSec())
            .equipmentInstallDate(row.getEquipmentInstallDate())
            .equipmentGrade(row.getEquipmentGrade())
            .equipmentWarrantyMonths(row.getEquipmentWarrantyMonths())
            .equipmentDesignLifeMonths(row.getEquipmentDesignLifeMonths())
            .equipmentWearCoefficient(row.getEquipmentWearCoefficient())
            .maintenanceWeightedScoreSum(row.getMaintenanceWeightedScoreSum())
            .maintenanceWeightSum(row.getMaintenanceWeightSum())
            .environmentTemperature(row.getEnvironmentTemperature())
            .environmentHumidity(row.getEnvironmentHumidity())
            .environmentParticleCount(row.getEnvironmentParticleCount())
            .environmentTempMin(row.getEnvironmentTempMin())
            .environmentTempMax(row.getEnvironmentTempMax())
            .environmentHumidityMin(row.getEnvironmentHumidityMin())
            .environmentHumidityMax(row.getEnvironmentHumidityMax())
            .environmentParticleLimit(row.getEnvironmentParticleLimit())
            .environmentTempWeight(row.getEnvironmentTempWeight())
            .environmentHumidityWeight(row.getEnvironmentHumidityWeight())
            .environmentParticleWeight(row.getEnvironmentParticleWeight())
            .defectiveWorkersSameLot(row.getDefectiveWorkersSameLot())
            .totalWorkersSameLot(row.getTotalWorkersSameLot())
            .lotDefectThreshold(row.getLotDefectThreshold())
            .difficultyScore(row.getDifficultyScore())
            .difficultyGrade(row.getDifficultyGrade())
            .currentSkillTier(row.getCurrentSkillTier())
            .errorReferenceRate(row.getErrorReferenceRate())
            .baselineError(row.getBaselineError())
            .environmentCorrection(row.getEnvironmentCorrection())
            .materialCorrection(row.getMaterialCorrection())
            .antiGamingPenalty(row.getAntiGamingPenalty())
            .groupMean(row.getGroupMean())
            .groupStdDev(row.getGroupStdDev())
            .actualError(row.getActualError())
            .build();
    }

    private BatchPeriodType parsePeriodType(String value) {
        if (value == null || value.isBlank()) {
            return BatchPeriodType.MONTH;
        }
        return BatchPeriodType.valueOf(value.trim().toUpperCase());
    }
}
