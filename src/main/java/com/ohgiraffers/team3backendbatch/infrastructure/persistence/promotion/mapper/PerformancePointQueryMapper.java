package com.ohgiraffers.team3backendbatch.infrastructure.persistence.promotion.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PerformancePointQueryMapper {

    List<EmployeePerformancePointTotalRow> findEmployeePointTotals();

    List<EmployeePerformancePointTotalRow> findEmployeePointTotalsByEarnedDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
