package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.entity.MesQualityResultProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesQualityResultProjectionRepository extends JpaRepository<MesQualityResultProjectionEntity, Long> {
}
