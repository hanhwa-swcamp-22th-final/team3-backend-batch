package com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.writer;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.analysis.model.QualitativeAnalysisResult;
import com.ohgiraffers.team3backendbatch.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.QualitativeEvaluationAnalyzedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.publisher.QualitativeAnalysisEventPublisher;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.bias.entity.BiasCorrectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.bias.repository.BiasCorrectionRepository;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeEvaluationEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeEvaluationRepository;
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
    private final BiasCorrectionRepository biasCorrectionRepository;
    private final IdGenerator idGenerator;
    private final QualitativeAnalysisEventPublisher qualitativeAnalysisEventPublisher;

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

        for (QualitativeEvaluationEntity evaluation : evaluations) {
            QualitativeAnalysisResult result = resultById.get(evaluation.getQualitativeEvaluationId());
            evaluation.applyCalculatedResult(
                result.getSqualRaw(),
                null,
                null
            );

            if (result.isBiasCorrected()) {
                biasCorrections.add(toBiasCorrectionEntity(result));
            }
        }

        qualitativeEvaluationRepository.saveAll(evaluations);
        if (!biasCorrections.isEmpty()) {
            biasCorrectionRepository.saveAll(biasCorrections);
        }
        publishAnalyzedEventsAfterCommit(List.copyOf(chunk.getItems()));
        log.info(
            "Updated qualitative raw score only. itemCount={}, biasCorrectionCount={}",
            evaluations.size(),
            biasCorrections.size()
        );
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

    private <T> T requireValue(T value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException("Missing bias correction field: " + fieldName);
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