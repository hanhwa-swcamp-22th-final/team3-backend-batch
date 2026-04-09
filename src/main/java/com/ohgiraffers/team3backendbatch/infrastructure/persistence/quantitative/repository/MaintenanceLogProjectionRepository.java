package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MaintenanceLogProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceLogProjectionRepository extends JpaRepository<MaintenanceLogProjectionEntity, Long> {
}
