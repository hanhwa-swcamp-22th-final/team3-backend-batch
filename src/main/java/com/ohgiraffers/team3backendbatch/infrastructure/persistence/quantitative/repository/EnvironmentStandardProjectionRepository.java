package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EnvironmentStandardProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentStandardProjectionRepository extends JpaRepository<EnvironmentStandardProjectionEntity, Long> {
}
