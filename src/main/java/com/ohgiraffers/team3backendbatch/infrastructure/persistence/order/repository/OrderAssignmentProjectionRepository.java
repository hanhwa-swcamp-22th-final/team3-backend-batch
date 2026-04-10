package com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.entity.OrderAssignmentProjectionEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderAssignmentProjectionRepository extends JpaRepository<OrderAssignmentProjectionEntity, Long> {

    @Query(
        value = """
            SELECT
                oa.employee_id AS employeeId,
                COUNT(*) AS challengeCount
            FROM batch_projection.order_assignment_projection oa
            JOIN batch_projection.order_difficulty_projection od
              ON od.order_id = oa.order_id
            WHERE oa.employee_id IS NOT NULL
              AND oa.assigned_at >= :startAt
              AND oa.assigned_at < :endExclusive
              AND COALESCE(oa.matching_status, '') <> 'CANCELLED'
              AND od.difficulty_grade IN ('D4', 'D5')
            GROUP BY oa.employee_id
            ORDER BY oa.employee_id
            """,
        nativeQuery = true
    )
    List<EmployeeChallengeCountView> findChallengeCountsByAssignedAtBetween(
        @Param("startAt") LocalDateTime startAt,
        @Param("endExclusive") LocalDateTime endExclusive
    );

    interface EmployeeChallengeCountView {
        Long getEmployeeId();
        Integer getChallengeCount();
    }
}
