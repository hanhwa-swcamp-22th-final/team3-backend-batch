package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.PerformancePointProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformancePointProjectionRepository extends JpaRepository<PerformancePointProjectionEntity, Long> {
}
