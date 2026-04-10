package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesProductionResultProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesProductionResultProjectionRepository extends JpaRepository<MesProductionResultProjectionEntity, String> {
}
