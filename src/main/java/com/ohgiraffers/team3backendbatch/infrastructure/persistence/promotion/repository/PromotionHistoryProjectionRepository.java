package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.PromotionHistoryProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionHistoryProjectionRepository extends JpaRepository<PromotionHistoryProjectionEntity, Long> {
}
