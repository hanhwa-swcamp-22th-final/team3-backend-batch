package com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KmsArticleSnapshotEvent {

    private Long articleId;
    private Long authorId;
    private String articleStatus;
    private LocalDateTime approvedAt;
    private Boolean deleted;
    private LocalDateTime occurredAt;
}
