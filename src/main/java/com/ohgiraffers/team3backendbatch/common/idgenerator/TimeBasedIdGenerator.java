package com.ohgiraffers.team3backendbatch.common.idgenerator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class TimeBasedIdGenerator implements IdGenerator {

    private static final long RANDOM_BOUND = 1_000L;
    private static final long RANDOM_MIN = 100L;

    private final AtomicLong lastGeneratedId = new AtomicLong(0L);

    @Override
    public Long generate() {
        long candidate = createCandidate();

        return lastGeneratedId.updateAndGet(previousId ->
            Math.max(candidate, previousId + 1)
        );
    }

    private long createCandidate() {
        long millis = System.currentTimeMillis();
        long randomSuffix = ThreadLocalRandom.current().nextLong(RANDOM_MIN, RANDOM_BOUND);

        return (millis * 1000) + randomSuffix;
    }
}