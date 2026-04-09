package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EmployeeProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeProjectionRepository extends JpaRepository<EmployeeProjectionEntity, Long> {
}
