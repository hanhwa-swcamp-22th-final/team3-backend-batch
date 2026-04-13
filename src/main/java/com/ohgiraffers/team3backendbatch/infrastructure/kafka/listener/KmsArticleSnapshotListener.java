package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.KmsArticleSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.KmsKafkaTopics;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.entity.KmsArticleProjectionEntity;
import com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.repository.KmsArticleProjectionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KmsArticleSnapshotListener {

    private static final Logger log = LoggerFactory.getLogger(KmsArticleSnapshotListener.class);

    private final KmsArticleProjectionRepository repository;

    @KafkaListener(
        topics = KmsKafkaTopics.KMS_ARTICLE_SNAPSHOT,
        containerFactory = "kmsArticleSnapshotKafkaListenerContainerFactory"
    )
    public void listen(KmsArticleSnapshotEvent event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurredAt = event.getOccurredAt() == null ? now : event.getOccurredAt();

        KmsArticleProjectionEntity projection = repository.findById(event.getArticleId())
            .map(existing -> {
                existing.refreshSnapshot(
                    event.getAuthorId(),
                    event.getArticleStatus(),
                    event.getApprovedAt(),
                    event.getDeleted(),
                    occurredAt,
                    now
                );
                return existing;
            })
            .orElseGet(() -> KmsArticleProjectionEntity.create(
                event.getArticleId(),
                event.getAuthorId(),
                event.getArticleStatus(),
                event.getApprovedAt(),
                event.getDeleted(),
                occurredAt,
                now
            ));

        repository.save(projection);
        log.info(
            "Upserted KMS article projection. articleId={}, authorId={}, articleStatus={}, deleted={}",
            event.getArticleId(),
            event.getAuthorId(),
            event.getArticleStatus(),
            event.getDeleted()
        );
    }
}
