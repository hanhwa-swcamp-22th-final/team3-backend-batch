package com.ohgiraffers.team3backendbatch.infrastructure.client;

import java.time.LocalDate;
import java.util.Map;

/**
 * Reads period-based KMS contribution counts for score aggregation.
 */
public interface KmsBatchSourceGateway {

    Map<Long, Integer> getApprovedArticleCounts(LocalDate startDate, LocalDate endDate);
}
