package com.ohgiraffers.team3backendbatch.infrastructure.persistence.order.mapper;

import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultySource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderDifficultyQueryMapper {

    OrderDifficultySource findOrderDifficultySource(@Param("orderId") Long orderId);
}
