package com.ohgiraffers.team3backendbatch.batch.job.quantitative.writer;

import com.ohgiraffers.team3backendbatch.batch.job.quantitative.model.QuantitativeEvaluationAggregate;
import com.ohgiraffers.team3backendbatch.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.QuantitativeEvaluationEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository.QuantitativeEvaluationRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuantitativeEvaluationWriter implements ItemWriter<QuantitativeEvaluationAggregate> {

    private static final Logger log = LoggerFactory.getLogger(QuantitativeEvaluationWriter.class);

    private final QuantitativeEvaluationRepository quantitativeEvaluationRepository;
    private final IdGenerator idGenerator;

    @Override
    public void write(Chunk<? extends QuantitativeEvaluationAggregate> chunk) {
        List<QuantitativeEvaluationEntity> entities = new ArrayList<>();

        for (QuantitativeEvaluationAggregate item : chunk.getItems()) {
            QuantitativeEvaluationEntity entity = quantitativeEvaluationRepository
                .findByEmployeeIdAndEvaluationPeriodIdAndEquipmentId(
                    item.getEmployeeId(),
                    item.getEvaluationPeriodId(),
                    item.getEquipmentId()
                )
                .orElseGet(() -> QuantitativeEvaluationEntity.create(
                    idGenerator.generate(),
                    item.getEmployeeId(),
                    item.getEvaluationPeriodId(),
                    item.getEquipmentId()
                ));

            entity.applyCalculatedResult(item);
            entities.add(entity);
        }

        quantitativeEvaluationRepository.saveAll(entities);
        log.info("Upserted quantitative evaluations. itemCount={}", entities.size());
    }
}