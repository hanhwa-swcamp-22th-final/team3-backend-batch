package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.writer;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeSentenceAnalysisEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QualitativeAnalysisEventPublisher;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeScoreProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeScoreProjectionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class QualitativeAnalysisWriter implements ItemWriter<QualitativeAnalysisResult> {

    private static final Logger log = LoggerFactory.getLogger(QualitativeAnalysisWriter.class);

    private final QualitativeAnalysisEventPublisher qualitativeAnalysisEventPublisher;
    private final QualitativeScoreProjectionRepository qualitativeScoreProjectionRepository;

    @Override
    public void write(Chunk<? extends QualitativeAnalysisResult> chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        List<? extends QualitativeAnalysisResult> results = List.copyOf(chunk.getItems());
        updateProjection(results);
        publishAnalyzedEventsAfterCommit(results);
        log.info("Completed qualitative analysis, updated projection, and queued analyzed events. itemCount={}", chunk.size());
    }

    private void updateProjection(List<? extends QualitativeAnalysisResult> results) {
        List<Long> evaluationIds = results.stream()
            .map(QualitativeAnalysisResult::getEvaluationId)
            .toList();

        Map<Long, QualitativeAnalysisResult> resultById = results.stream()
            .collect(Collectors.toMap(QualitativeAnalysisResult::getEvaluationId, Function.identity()));

        List<QualitativeScoreProjectionEntity> projections = qualitativeScoreProjectionRepository
            .findAllByQualitativeEvaluationIdIn(evaluationIds);

        if (projections.size() != evaluationIds.size()) {
            throw new IllegalStateException("Some qualitative score projections were not found for analysis update.");
        }

        LocalDateTime now = LocalDateTime.now();
        for (QualitativeScoreProjectionEntity projection : projections) {
            QualitativeAnalysisResult result = resultById.get(projection.getQualitativeEvaluationId());
            LocalDateTime analyzedAt = result.getAnalyzedAt() == null ? now : result.getAnalyzedAt();
            projection.recordAnalysis(result.getSqualRaw(), result.getAnalysisStatus(), analyzedAt, now);
        }

        qualitativeScoreProjectionRepository.saveAll(projections);
    }

    private void publishAnalyzedEventsAfterCommit(List<? extends QualitativeAnalysisResult> results) {
        Runnable publishAction = () -> results.forEach(result ->
            qualitativeAnalysisEventPublisher.publishAnalyzed(
                new QualitativeEvaluationAnalyzedEvent(
                    result.getEvaluationId(),
                    result.getAlgorithmVersionId(),
                    result.getAnalysisStatus(),
                    result.getSqualRaw(),
                    result.getSQual(),
                    result.getNormalizedTier(),
                    result.getAnalyzedAt(),
                    toSentenceAnalysisEvents(result)
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
            return;
        }

        publishAction.run();
    }

    private List<QualitativeSentenceAnalysisEvent> toSentenceAnalysisEvents(QualitativeAnalysisResult result) {
        if (result.getSentenceAnalyses() == null || result.getSentenceAnalyses().isEmpty()) {
            return List.of();
        }

        return result.getSentenceAnalyses().stream()
            .map(sentenceAnalysis -> new QualitativeSentenceAnalysisEvent(
                sentenceAnalysis.getNlpSentiment(),
                sentenceAnalysis.getMatchedKeywordCount(),
                sentenceAnalysis.getMatchedKeywords(),
                sentenceAnalysis.getContextWeight(),
                sentenceAnalysis.isNegationDetected()
            ))
            .toList();
    }
}
