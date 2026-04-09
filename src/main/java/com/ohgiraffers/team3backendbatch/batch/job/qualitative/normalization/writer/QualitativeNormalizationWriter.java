package com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.writer;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.normalization.model.QualitativeNormalizationResult;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeEvaluationEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativeEvaluationRepository;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QualitativeNormalizationWriter implements ItemWriter<QualitativeNormalizationResult> {

    private final QualitativeEvaluationRepository qualitativeEvaluationRepository;

    @Override
    public void write(Chunk<? extends QualitativeNormalizationResult> chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        var evaluationIds = chunk.getItems().stream()
            .map(QualitativeNormalizationResult::getEvaluationId)
            .toList();

        Map<Long, QualitativeNormalizationResult> resultById = chunk.getItems().stream()
            .collect(Collectors.toMap(
                QualitativeNormalizationResult::getEvaluationId,
                Function.identity()
            ));

        var evaluations = qualitativeEvaluationRepository.findAllByQualitativeEvaluationIdIn(evaluationIds);
        if (evaluations.size() != evaluationIds.size()) {
            throw new IllegalStateException("Some qualitative evaluations were not found for normalization update.");
        }

        for (QualitativeEvaluationEntity evaluation : evaluations) {
            QualitativeNormalizationResult result = resultById.get(evaluation.getQualitativeEvaluationId());
            evaluation.applyCalculatedResult(
                result.getRawScore(),
                result.getSQual(),
                result.getGrade()
            );
        }

        qualitativeEvaluationRepository.saveAll(evaluations);
        log.info("Updated qualitative normalized score. itemCount={}", evaluations.size());
    }
}