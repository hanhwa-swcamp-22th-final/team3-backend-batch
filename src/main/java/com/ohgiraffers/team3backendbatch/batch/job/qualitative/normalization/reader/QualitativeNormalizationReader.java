package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.reader;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationTarget;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.mapper.QualitativeScoreQueryMapper;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
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

    private final QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;
    private final QualitativeScoreQueryMapper qualitativeScoreQueryMapper;
    private final Long requestedEvaluationPeriodId;
    private final boolean force;
    private Long resolvedEvaluationPeriodId;
    private Iterator<QualitativeNormalizationTarget> iterator;

    /**
     * 정성 점수 정규화 Reader 를 생성한다.
     * @param qualitativeScoreProjectionRepository 정성 점수 projection 저장소
     * @param qualitativeScoreQueryMapper 정성 점수 조회 mapper
     * @param evaluationPeriodId 평가 기간 ID
     * @param force 강제 재계산 여부
     */
    public QualitativeNormalizationReader(
        QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository,
        QualitativeScoreQueryMapper qualitativeScoreQueryMapper,
        @Value("#{jobParameters['evaluationPeriodId']}") Long evaluationPeriodId,
        @Value("#{jobParameters['force']}") String force
    ) {
        this.qualitativeScoreProjectionRepository = qualitativeScoreProjectionRepository;
        this.qualitativeScoreQueryMapper = qualitativeScoreQueryMapper;
        this.requestedEvaluationPeriodId = evaluationPeriodId;
        this.force = Boolean.parseBoolean(force);
    }

    /**
     * 정성 점수 정규화 대상을 한 건씩 반환한다.
     * @param 없음
     * @return 정성 점수 정규화 대상 데이터
     */
    @Override
    public QualitativeNormalizationTarget read() {
        if (iterator == null) {
            resolvedEvaluationPeriodId = requestedEvaluationPeriodId != null
                ? requestedEvaluationPeriodId
                : qualitativeScoreQueryMapper.findLatestEvaluationPeriodIdForNormalization();

            if (resolvedEvaluationPeriodId == null) {
                log.info(
                    "No qualitative normalization target period found. requestedEvaluationPeriodId={}, force={}",
                    requestedEvaluationPeriodId,
                    force
                );
                iterator = List.<QualitativeNormalizationTarget>of().iterator();
                return null;
            }

            List<QualitativeScoreProjectionEntity> projections = force
                ? qualitativeScoreProjectionRepository.findByEvaluationPeriodIdAndRawScoreIsNotNullOrderByEvaluationLevelAscQualitativeEvaluationIdAsc(
                    resolvedEvaluationPeriodId
                )
                : qualitativeScoreProjectionRepository.findByEvaluationPeriodIdAndRawScoreIsNotNullAndNormalizedScoreIsNullOrderByEvaluationLevelAscQualitativeEvaluationIdAsc(
                    resolvedEvaluationPeriodId
                );

            List<QualitativeNormalizationTarget> items = projections.stream()
                .map(projection -> new QualitativeNormalizationTarget(
                    projection.getQualitativeEvaluationId(),
                    projection.getEvaluatorId(),
                    projection.getRawScore()
                ))
                .toList();

            log.info(
                "Loaded qualitative normalization targets from projection. requestedEvaluationPeriodId={}, resolvedEvaluationPeriodId={}, force={}, count={}",
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
