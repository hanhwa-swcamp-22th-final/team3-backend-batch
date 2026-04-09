package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.reader;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationTarget;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeEvaluationQueryMapper;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class QualitativeNormalizationReader implements ItemReader<QualitativeNormalizationTarget> {

    private final QualitativeEvaluationQueryMapper qualitativeEvaluationQueryMapper;
    private final Long requestedEvaluationPeriodId;
    private final boolean force;
    private Long resolvedEvaluationPeriodId;
    private Iterator<QualitativeNormalizationTarget> iterator;

    public QualitativeNormalizationReader(
        QualitativeEvaluationQueryMapper qualitativeEvaluationQueryMapper,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId,
        @Value("#{jobParameters['force']}") String force
    ) {
        this.qualitativeEvaluationQueryMapper = qualitativeEvaluationQueryMapper;
        this.requestedEvaluationPeriodId = evaluationPeriodId;
        this.force = Boolean.parseBoolean(force);
    }

    @Override
    public QualitativeNormalizationTarget read() {
        if (iterator == null) {
            resolvedEvaluationPeriodId = requestedEvaluationPeriodId != null
                ? requestedEvaluationPeriodId
                : qualitativeEvaluationQueryMapper.findLatestEvaluationPeriodIdForNormalization();

            if (resolvedEvaluationPeriodId == null) {
                log.info(
                    "No qualitative normalization target period found. requestedEvaluationPeriodId={}, force={}",
                    requestedEvaluationPeriodId,
                    force
                );
                iterator = List.<QualitativeNormalizationTarget>of().iterator();
                return null;
            }

            List<QualitativeNormalizationTarget> items = qualitativeEvaluationQueryMapper
                .findQualitativeEvaluationsForNormalization(resolvedEvaluationPeriodId, force);
            log.info(
                "Loaded qualitative normalization targets. requestedEvaluationPeriodId={}, resolvedEvaluationPeriodId={}, force={}, count={}",
                requestedEvaluationPeriodId,
                resolvedEvaluationPeriodId,
                force,
                items.size()
            );
            iterator = items.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}