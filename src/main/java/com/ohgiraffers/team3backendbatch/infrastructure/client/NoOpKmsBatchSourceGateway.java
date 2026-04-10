package com.ohgiraffers.team3backendbatch.infrastructure.client;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NoOpKmsBatchSourceGateway implements KmsBatchSourceGateway {

    @Override
    public Map<Long, Integer> getApprovedArticleCounts(LocalDate startDate, LocalDate endDate) {
        return Map.of();
    }
}
