package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesEquipmentStatusProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesEquipmentStatusProjectionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesEquipmentStatusProjectionRepository
    extends JpaRepository<MesEquipmentStatusProjectionEntity, MesEquipmentStatusProjectionId> {
}
