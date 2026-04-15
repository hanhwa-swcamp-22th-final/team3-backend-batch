package com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderAssignmentQueryMapper {

    List<EmployeeChallengeCountRow> findChallengeCountsByAssignedAtBetween(
        @Param("startAt") LocalDateTime startAt,
        @Param("endExclusive") LocalDateTime endExclusive
    );
}
