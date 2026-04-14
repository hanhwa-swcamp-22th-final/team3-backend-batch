package com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(catalog = "batch_projection", name = "kms_article_projection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KmsArticleProjectionEntity {

    private static final long SYSTEM_ACTOR_ID = 0L;

    @Id
    @Column(name = "article_id")
    private Long articleId;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "article_status")
    private String articleStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    public static KmsArticleProjectionEntity create(
        Long articleId,
        Long authorId,
        String articleStatus,
        LocalDateTime approvedAt,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        KmsArticleProjectionEntity entity = new KmsArticleProjectionEntity();
        entity.articleId = articleId;
        entity.authorId = authorId;
        entity.articleStatus = articleStatus;
        entity.approvedAt = approvedAt;
        entity.deleted = deleted;
        entity.occurredAt = occurredAt;
        entity.createdAt = now;
        entity.createdBy = SYSTEM_ACTOR_ID;
        entity.updatedAt = now;
        entity.updatedBy = SYSTEM_ACTOR_ID;
        return entity;
    }

    public void refreshSnapshot(
        Long authorId,
        String articleStatus,
        LocalDateTime approvedAt,
        Boolean deleted,
        LocalDateTime occurredAt,
        LocalDateTime now
    ) {
        this.authorId = authorId;
        this.articleStatus = articleStatus;
        this.approvedAt = approvedAt;
        this.deleted = deleted;
        this.occurredAt = occurredAt;
        this.updatedAt = now;
        this.updatedBy = SYSTEM_ACTOR_ID;
    }
}
