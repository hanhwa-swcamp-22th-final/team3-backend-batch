package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.writer;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MatchedKeywordDetailEvent;
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

    /**
     * 정성 평가 분석 결과를 projection 에 반영하고 이벤트를 발행한다.
     * @param chunk 처리할 정성 평가 분석 결과 묶음
     * @return 반환값 없음
     */
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

    /**
     * 정성 평가 분석 결과를 projection 테이블에 반영한다.
     * @param results 정성 평가 분석 결과 목록
     * @return 반환값 없음
     */
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

    /**
     * 트랜잭션 커밋 이후 정성 평가 분석 이벤트를 발행한다.
     * @param results 발행할 정성 평가 분석 결과 목록
     * @return 반환값 없음
     */
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

    /**
     * 문장 단위 분석 결과를 이벤트 payload 목록으로 변환한다.
     * @param result 정성 평가 분석 결과
     * @return 문장 단위 분석 이벤트 목록
     */
    private List<QualitativeSentenceAnalysisEvent> toSentenceAnalysisEvents(QualitativeAnalysisResult result) {
        if (result.getSentenceAnalyses() == null || result.getSentenceAnalyses().isEmpty()) {
            return List.of();
        }

        return result.getSentenceAnalyses().stream()
            .map(sentenceAnalysis -> new QualitativeSentenceAnalysisEvent(
                sentenceAnalysis.getNlpSentiment(),
                sentenceAnalysis.getMatchedKeywordCount(),
                sentenceAnalysis.getMatchedKeywords(),
                sentenceAnalysis.getMatchedKeywordDetails().stream()
                    .map(detail -> new MatchedKeywordDetailEvent(
                        detail.getDomainKeywordId(),
                        detail.getKeyword(),
                        detail.getDomainCompetencyCategory(),
                        detail.getScoreWeight()
                    ))
                    .toList(),
                sentenceAnalysis.getContextWeight(),
                sentenceAnalysis.isNegationDetected()
            ))
            .toList();
    }
}
