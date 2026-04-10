package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.entity.PerformancePointProjectionEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PerformancePointProjectionRepository extends JpaRepository<PerformancePointProjectionEntity, Long> {

    @Query(
        value = """
            SELECT
                p.performance_employee_id AS employeeId,
                COALESCE(SUM(p.point_amount), 0) AS totalPoint
            FROM batch_projection.performance_point_projection p
            GROUP BY p.performance_employee_id
            """,
        nativeQuery = true
    )
    List<EmployeePerformancePointTotalView> findEmployeePointTotals();

    @Query(
        value = """
            SELECT
                p.performance_employee_id AS employeeId,
                COALESCE(SUM(p.point_amount), 0) AS totalPoint
            FROM batch_projection.performance_point_projection p
            WHERE p.point_earned_date >= :startDate
              AND p.point_earned_date <= :endDate
            GROUP BY p.performance_employee_id
            """,
        nativeQuery = true
    )
    List<EmployeePerformancePointTotalView> findEmployeePointTotalsByEarnedDateBetween(
        LocalDate startDate,
        LocalDate endDate
    );

    interface EmployeePerformancePointTotalView {
        Long getEmployeeId();
        BigDecimal getTotalPoint();
    }
}
