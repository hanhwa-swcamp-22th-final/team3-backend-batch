package com.ohgiraffers.team3backendbatch.infrastructure.persistence.quantitative.mapper;

import com.ohgiraffers.team3backendbatch.api.command.dto.BatchPeriodType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EvaluationPeriodQueryMapper {

    EvaluationPeriodProjectionRow findLatestConfirmedPeriodByInclusiveDaysBetween(
        @Param("minDays") int minDays,
        @Param("maxDays") int maxDays
    );

    List<EvaluationPeriodProjectionRow> findConfirmedMonthlyPeriodsWithin(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    default Optional<EvaluationPeriodProjectionRow> findLatestConfirmedMonthlyPeriod() {
        return Optional.ofNullable(findLatestConfirmedPeriodByInclusiveDaysBetween(28, 31));
    }

    default Optional<EvaluationPeriodProjectionRow> findLatestConfirmedPeriod(BatchPeriodType periodType) {
        return Optional.ofNullable(switch (periodType) {
            case WEEK -> findLatestConfirmedPeriodByInclusiveDaysBetween(5, 8);
            case MONTH -> findLatestConfirmedPeriodByInclusiveDaysBetween(28, 31);
            case QUARTER -> findLatestConfirmedPeriodByInclusiveDaysBetween(89, 93);
            case HALF_YEAR -> findLatestConfirmedPeriodByInclusiveDaysBetween(180, 184);
            case YEAR -> findLatestConfirmedPeriodByInclusiveDaysBetween(364, 366);
        });
    }
}
