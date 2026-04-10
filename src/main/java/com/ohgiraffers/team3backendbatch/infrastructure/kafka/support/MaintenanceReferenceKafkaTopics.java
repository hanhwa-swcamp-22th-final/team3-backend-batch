package com.ohgiraffers.team3backendbatch.infrastructure.kafka.support;

public final class MaintenanceReferenceKafkaTopics {

    public static final String MAINTENANCE_LOG_SNAPSHOT = "admin.maintenance-log.snapshot";
    public static final String MAINTENANCE_ITEM_STANDARD_SNAPSHOT = "admin.maintenance-item-standard.snapshot";

    private MaintenanceReferenceKafkaTopics() {
    }
}
