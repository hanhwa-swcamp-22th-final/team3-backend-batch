package com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.qualitative.entity.EvaluationCommentEntity;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationCommentRepository extends JpaRepository<EvaluationCommentEntity, Long> {

    void deleteByQualitativeEvaluationIdIn(Collection<Long> qualitativeEvaluationIds);
}