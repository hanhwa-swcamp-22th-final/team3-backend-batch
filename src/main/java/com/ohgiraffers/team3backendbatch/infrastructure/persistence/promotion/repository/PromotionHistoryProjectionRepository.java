package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.PromotionHistoryProjectionEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionHistoryProjectionRepository extends JpaRepository<PromotionHistoryProjectionEntity, Long> {

    @Query("""
        SELECT DISTINCT p.employeeId
        FROM PromotionHistoryProjectionEntity p
        WHERE p.tierPromoStatus IN :statuses
        """)
    List<Long> findPendingEmployeeIds(@Param("statuses") Collection<String> statuses);
}
