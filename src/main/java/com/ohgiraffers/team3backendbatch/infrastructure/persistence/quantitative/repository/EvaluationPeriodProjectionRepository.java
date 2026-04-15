package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EvaluationPeriodProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationPeriodProjectionRepository extends JpaRepository<EvaluationPeriodProjectionEntity, Long> {
}
