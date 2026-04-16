package com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.writer;

import com.ohgiraffers.team3backendbatch.batch.job.qualitative.summary.model.QualitativePeriodSummaryResult;
import com.ohgiraffers.team3backendbatch.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativePeriodSummaryProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository.QualitativePeriodSummaryProjectionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QualitativePeriodSummaryWriter implements ItemWriter<QualitativePeriodSummaryResult> {

    private final QualitativePeriodSummaryProjectionRepository qualitativePeriodSummaryProjectionRepository;
    private final IdGenerator idGenerator;

    /**
     * 정성 기간 요약 결과를 projection 에 저장한다.
     * @param chunk 정성 기간 요약 결과 청크
     * @return 반환값 없음
     */
    @Override
    public void write(Chunk<? extends QualitativePeriodSummaryResult> chunk) {
        LocalDateTime now = LocalDateTime.now();
        List<QualitativePeriodSummaryProjectionEntity> entities = new ArrayList<>();

        for (QualitativePeriodSummaryResult result : chunk.getItems()) {
            QualitativePeriodSummaryProjectionEntity entity = qualitativePeriodSummaryProjectionRepository
                .findByEvaluationPeriodIdAndEvaluateeId(result.getEvaluationPeriodId(), result.getEvaluateeId())
                .orElseGet(() -> QualitativePeriodSummaryProjectionEntity.create(
                    idGenerator.generate(),
                    result.getEvaluationPeriodId(),
                    result.getEvaluateeId(),
                    result.getPeriodType(),
                    result.getSourceMonthCount(),
                    result.getAverageRawScore(),
                    result.getAverageNormalizedScore(),
                    result.getGrade(),
                    now,
                    now
                ));

            entity.applySummary(
                result.getPeriodType(),
                result.getSourceMonthCount(),
                result.getAverageRawScore(),
                result.getAverageNormalizedScore(),
                result.getGrade(),
                now,
                now
            );
            entities.add(entity);
        }

        qualitativePeriodSummaryProjectionRepository.saveAll(entities);
        log.info("Upserted qualitative period summaries. itemCount={}", entities.size());
    }
}
