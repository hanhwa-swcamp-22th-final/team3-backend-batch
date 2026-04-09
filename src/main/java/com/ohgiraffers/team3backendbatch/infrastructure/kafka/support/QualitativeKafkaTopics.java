package com.ohgiraffers.team3backendbatch.infrastructure.kafka.support;

public final class QualitativeKafkaTopics {

    public static final String QUALITATIVE_EVALUATION_SUBMITTED = "hr.qualitative-evaluation.submitted";
    public static final String QUALITATIVE_EVALUATION_ANALYZED = "batch.qualitative-evaluation.analyzed";
    public static final String QUALITATIVE_EVALUATION_NORMALIZED = "batch.qualitative-evaluation.normalized";

    private QualitativeKafkaTopics() {
    }
}
