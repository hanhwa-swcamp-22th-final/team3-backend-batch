package com.ohgiraffers.team3backendbatch.infrastructure.kafka.support;

public final class PromotionKafkaTopics {

    public static final String PERFORMANCE_POINT_CALCULATED = "batch.performance-point.calculated";
    public static final String PERFORMANCE_POINT_SNAPSHOT = "hr.performance-point.snapshot";
    public static final String PROMOTION_CANDIDATE_EVALUATED = "batch.promotion-candidate.evaluated";
    public static final String PROMOTION_HISTORY_SNAPSHOT = "hr.promotion-history.snapshot";
    public static final String TIER_CONFIG_SNAPSHOT = "hr.tier-config.snapshot";
    public static final String EVALUATION_WEIGHT_CONFIG_SNAPSHOT = "hr.evaluation-weight-config.snapshot";

    private PromotionKafkaTopics() {
    }
}
