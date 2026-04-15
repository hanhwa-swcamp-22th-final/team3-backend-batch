package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.EvaluationWeightConfigProjectionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationWeightConfigProjectionRepository extends JpaRepository<EvaluationWeightConfigProjectionEntity, Long> {

    List<EvaluationWeightConfigProjectionEntity> findAllByActiveTrueAndDeletedFalse();
}
