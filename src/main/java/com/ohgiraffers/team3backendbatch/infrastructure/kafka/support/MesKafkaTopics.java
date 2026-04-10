package com.ohgiraffers.team3backendbatch.infrastructure.kafka.support;

public final class MesKafkaTopics {

    public static final String MES_PRODUCTION_RESULT = "mes.production-result.raw";
    public static final String MES_QUALITY_RESULT = "mes.quality-result.raw";
    public static final String MES_QUALITY_MEASUREMENT = "mes.quality-measurement.raw";
    public static final String MES_EQUIPMENT_STATUS = "mes.equipment-status.raw";
    public static final String MES_ENVIRONMENT = "mes.environment.raw";

    private MesKafkaTopics() {
    }
}
