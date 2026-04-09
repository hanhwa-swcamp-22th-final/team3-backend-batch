package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.QuantitativeEvaluationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuantitativeEvaluationRepository extends JpaRepository<QuantitativeEvaluationEntity, Long> {

    Optional<QuantitativeEvaluationEntity> findByEmployeeIdAndEvaluationPeriodIdAndEquipmentId(
        Long employeeId,
        Long evaluationPeriodId,
        Long equipmentId
    );
}