package com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.entity.OrderAssignmentProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAssignmentProjectionRepository extends JpaRepository<OrderAssignmentProjectionEntity, Long> {
}
