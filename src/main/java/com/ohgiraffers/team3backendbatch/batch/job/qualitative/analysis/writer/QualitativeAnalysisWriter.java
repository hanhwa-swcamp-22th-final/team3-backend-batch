package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QualitativeAnalysisEventPublisher;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.bias.entity.BiasCorrectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.bias.repository.BiasCorrectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.EvaluationCommentEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeEvaluationEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.EvaluationCommentRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeEvaluationRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final QualitativeEvaluationRepository qualitativeEvaluationRepository;
    private final EvaluationCommentRepository evaluationCommentRepository;
    private final BiasCorrectionRepository biasCorrectionRepository;
    private final IdGenerator idGenerator;
    private final QualitativeAnalysisEventPublisher qualitativeAnalysisEventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void write(Chunk<? extends QualitativeAnalysisResult> chunk) {
        List<Long> evaluationIds = chunk.getItems().stream()
            .map(QualitativeAnalysisResult::getEvaluationId)
            .toList();

        Map<Long, QualitativeAnalysisResult> resultById = chunk.getItems().stream()
            .collect(Collectors.toMap(
                QualitativeAnalysisResult::getEvaluationId,
                Function.identity()
            ));

        List<QualitativeEvaluationEntity> evaluations =
            qualitativeEvaluationRepository.findAllByQualitativeEvaluationIdIn(evaluationIds);

        if (evaluations.size() != evaluationIds.size()) {
            throw new IllegalStateException("Some qualitative evaluations were not found for update.");
        }

        List<BiasCorrectionEntity> biasCorrections = new ArrayList<>();
        List<EvaluationCommentEntity> evaluationComments = new ArrayList<>();

        for (QualitativeEvaluationEntity evaluation : evaluations) {
            QualitativeAnalysisResult result = resultById.get(evaluation.getQualitativeEvaluationId());
            evaluation.applyCalculatedResult(
                result.getSqualRaw(),
                null,
                null
            );
            evaluationComments.addAll(toEvaluationCommentEntities(result));

            if (result.isBiasCorrected()) {
                biasCorrections.add(toBiasCorrectionEntity(result));
            }
        }

        qualitativeEvaluationRepository.saveAll(evaluations);
        evaluationCommentRepository.deleteByQualitativeEvaluationIdIn(evaluationIds);
        if (!evaluationComments.isEmpty()) {
            evaluationCommentRepository.saveAll(evaluationComments);
        }
        if (!biasCorrections.isEmpty()) {
            biasCorrectionRepository.saveAll(biasCorrections);
        }
        publishAnalyzedEventsAfterCommit(List.copyOf(chunk.getItems()));
        log.info(
            "Updated qualitative raw score and saved evaluation comments. itemCount={}, commentRowCount={}, biasCorrectionCount={}",
            evaluations.size(),
            evaluationComments.size(),
            biasCorrections.size()
        );
    }

    private List<EvaluationCommentEntity> toEvaluationCommentEntities(QualitativeAnalysisResult result) {
        if (result.getSentenceAnalyses() == null || result.getSentenceAnalyses().isEmpty()) {
            return List.of();
        }

        Long algorithmVersionId = requireValue(result.getAlgorithmVersionId(), "algorithmVersionId");
        LocalDateTime createdAt = requireValue(result.getAnalyzedAt(), "analyzedAt");

        return result.getSentenceAnalyses().stream()
            .map(sentenceAnalysis -> EvaluationCommentEntity.builder()
                .evaluationCommentId(idGenerator.generate())
                .qualitativeEvaluationId(result.getEvaluationId())
                .algorithmVersionId(algorithmVersionId)
                .nlpSentiment(sentenceAnalysis.getNlpSentiment())
                .matchedKeywordCount(sentenceAnalysis.getMatchedKeywordCount())
                .matchedKeywords(toJson(sentenceAnalysis.getMatchedKeywords()))
                .contextWeight(sentenceAnalysis.getContextWeight())
                .negationDetected(sentenceAnalysis.isNegationDetected())
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build())
            .toList();
    }

    private BiasCorrectionEntity toBiasCorrectionEntity(QualitativeAnalysisResult result) {
        return BiasCorrectionEntity.builder()
            .biasCorrectionId(idGenerator.generate())
            .evaluatorId(result.getEvaluatorId())
            .qualitativeEvaluationId(result.getEvaluationId())
            .biasType(requireValue(result.getBiasType(), "biasType"))
            .evaluatorAvg(requireValue(result.getEvaluatorAverage(), "evaluatorAverage"))
            .companyAvg(requireValue(result.getCompanyAverage(), "companyAverage"))
            .alphaBias(requireValue(result.getAlphaBias(), "alphaBias"))
            .originalScore(requireValue(result.getOriginalSQual(), "originalSQual"))
            .correctedScore(requireValue(result.getSQual(), "sQual"))
            .notificationSent(Boolean.FALSE)
            .detectedAt(requireValue(result.getAnalyzedAt(), "analyzedAt"))
            .build();
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize matched keywords.", exception);
        }
    }

    private <T> T requireValue(T value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException("Missing required field: " + fieldName);
        }
        return value;
    }

    private void publishAnalyzedEventsAfterCommit(List<? extends QualitativeAnalysisResult> results) {
        Runnable publishAction = () -> results.forEach(result ->
            qualitativeAnalysisEventPublisher.publishAnalyzed(
                new QualitativeEvaluationAnalyzedEvent(
                    result.getEvaluationId(),
                    result.getAnalysisStatus(),
                    result.getSQual(),
                    result.getNormalizedTier(),
                    result.getAnalyzedAt()
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
}