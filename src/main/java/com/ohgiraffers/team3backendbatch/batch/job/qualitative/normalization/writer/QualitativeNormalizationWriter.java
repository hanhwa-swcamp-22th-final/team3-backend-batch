package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.writer;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationResult;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationNormalizedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QualitativeNormalizationEventPublisher;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class QualitativeNormalizationWriter implements ItemWriter<QualitativeNormalizationResult> {

    private final QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;
    private final QualitativeNormalizationEventPublisher qualitativeNormalizationEventPublisher;

    /**
     * 정규화 결과를 projection 에 반영하고 이벤트를 발행한다.
     * @param chunk 처리할 정규화 결과 묶음
     * @return 반환값 없음
     */
    @Override
    public void write(Chunk<? extends QualitativeNormalizationResult> chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        List<Long> evaluationIds = chunk.getItems().stream()
            .map(QualitativeNormalizationResult::getEvaluationId)
            .toList();

        Map<Long, QualitativeNormalizationResult> resultById = chunk.getItems().stream()
            .collect(Collectors.toMap(
                QualitativeNormalizationResult::getEvaluationId,
                Function.identity()
            ));

        List<QualitativeNormalizationResult> results = List.copyOf(chunk.getItems());
        updateProjection(resultById, evaluationIds);
        publishNormalizedEventsAfterCommit(results);
    }

    /**
     * 정규화 결과를 projection 테이블에 반영한다.
     * @param resultById 평가 ID 별 정규화 결과 맵
     * @param evaluationIds 반영할 평가 ID 목록
     * @return 반환값 없음
     */
    private void updateProjection(Map<Long, QualitativeNormalizationResult> resultById, List<Long> evaluationIds) {
        List<QualitativeScoreProjectionEntity> projections = qualitativeScoreProjectionRepository
            .findAllByQualitativeEvaluationIdIn(evaluationIds);
        if (projections.size() != evaluationIds.size()) {
            throw new IllegalStateException("Some qualitative score projections were not found for normalization update.");
        }

        LocalDateTime now = LocalDateTime.now();
        for (QualitativeScoreProjectionEntity projection : projections) {
            QualitativeNormalizationResult result = resultById.get(projection.getQualitativeEvaluationId());
            projection.recordNormalization(result.getSQual(), result.getGrade(), now, now);
        }
        qualitativeScoreProjectionRepository.saveAll(projections);
    }

    /**
     * 트랜잭션 커밋 이후 정규화 완료 이벤트를 발행한다.
     * @param results 발행할 정규화 결과 목록
     * @return 반환값 없음
     */
    private void publishNormalizedEventsAfterCommit(List<QualitativeNormalizationResult> results) {
        Runnable publishAction = () -> results.forEach(result ->
            qualitativeNormalizationEventPublisher.publishNormalized(
                new QualitativeEvaluationNormalizedEvent(
                    result.getEvaluationId(),
                    result.getRawScore(),
                    result.getSQual(),
                    result.getGrade(),
                    LocalDateTime.now()
                )
            )
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAction.run();
                }
            });
            log.info("Updated qualitative normalization projection and queued normalized events. itemCount={}", results.size());
            return;
        }

        publishAction.run();
        log.info("Updated qualitative normalization projection and queued normalized events. itemCount={}", results.size());
    }
}
