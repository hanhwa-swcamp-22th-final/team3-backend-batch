package com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.repository;

import com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.entity.KmsArticleProjectionEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KmsArticleProjectionRepository extends JpaRepository<KmsArticleProjectionEntity, Long> {

    @Query(
        value = """
            SELECT
                ka.author_id AS employeeId,
                COUNT(*) AS approvedArticleCount
            FROM batch_projection.kms_article_projection ka
            WHERE ka.author_id IS NOT NULL
              AND ka.article_status = 'APPROVED'
              AND COALESCE(ka.deleted, 0) = 0
              AND ka.approved_at >= :startAt
              AND ka.approved_at < :endExclusive
            GROUP BY ka.author_id
            ORDER BY ka.author_id
            """,
        nativeQuery = true
    )
    List<EmployeeApprovedArticleCountView> findApprovedArticleCountsByApprovedAtBetween(
        @Param("startAt") LocalDateTime startAt,
        @Param("endExclusive") LocalDateTime endExclusive
    );

    interface EmployeeApprovedArticleCountView {
        Long getEmployeeId();
        Long getApprovedArticleCount();
    }
}
