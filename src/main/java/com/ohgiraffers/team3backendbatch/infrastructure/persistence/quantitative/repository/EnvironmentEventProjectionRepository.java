package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EnvironmentEventProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentEventProjectionRepository
    extends JpaRepository<EnvironmentEventProjectionEntity, String> {
}
