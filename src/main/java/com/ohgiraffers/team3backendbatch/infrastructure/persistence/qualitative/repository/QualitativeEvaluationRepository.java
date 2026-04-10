package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.QualitativeEvaluationEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualitativeEvaluationRepository extends JpaRepository<QualitativeEvaluationEntity, Long> {

    List<QualitativeEvaluationEntity> findAllByQualitativeEvaluationIdIn(Collection<Long> qualitativeEvaluationIds);
}