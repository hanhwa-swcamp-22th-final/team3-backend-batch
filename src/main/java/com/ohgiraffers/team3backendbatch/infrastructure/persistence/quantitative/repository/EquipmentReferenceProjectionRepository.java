package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.EquipmentReferenceProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentReferenceProjectionRepository extends JpaRepository<EquipmentReferenceProjectionEntity, Long> {
}
