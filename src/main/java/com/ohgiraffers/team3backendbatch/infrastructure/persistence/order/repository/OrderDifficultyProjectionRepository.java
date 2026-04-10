package com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.entity.OrderDifficultyProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDifficultyProjectionRepository extends JpaRepository<OrderDifficultyProjectionEntity, Long> {
}
