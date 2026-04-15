package com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.entity.KmsArticleProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KmsArticleProjectionRepository extends JpaRepository<KmsArticleProjectionEntity, Long> {
}
