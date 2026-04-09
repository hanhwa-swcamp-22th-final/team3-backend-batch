package com.ohgiraffers.team3backendbatch.infrastructure.persistence.bias.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.bias.entity.BiasCorrectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiasCorrectionRepository extends JpaRepository<BiasCorrectionEntity, Long> {
}