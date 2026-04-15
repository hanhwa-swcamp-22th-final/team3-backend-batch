package com.ohgiraffers.team3backendbatch.infrastructure.persistence.kms.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KmsArticleQueryMapper {

    List<KmsApprovedArticleCountRow> findApprovedArticleCountsByApprovedAtBetween(
        @Param("startAt") LocalDateTime startAt,
        @Param("endExclusive") LocalDateTime endExclusive
    );
}
