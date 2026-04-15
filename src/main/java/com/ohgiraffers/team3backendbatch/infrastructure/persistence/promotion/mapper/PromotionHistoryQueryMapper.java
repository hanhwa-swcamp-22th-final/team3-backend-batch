package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PromotionHistoryQueryMapper {

    List<Long> findPendingEmployeeIds(@Param("statuses") Collection<String> statuses);
}
