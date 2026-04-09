package com.ohgiraffers.team3backendbatch.infrastructure.kafka.config;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MaintenanceItemStandardSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MaintenanceLogSnapshotEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@EnableKafka
public class MaintenanceReferenceKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, MaintenanceLogSnapshotEvent> maintenanceLogSnapshotConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-maintenance-log");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<MaintenanceLogSnapshotEvent> deserializer =
            new JsonDeserializer<>(MaintenanceLogSnapshotEvent.class);
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages(
            "com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto",
            "com.ohgiraffers.team3backendadmin.infrastructure.kafka.dto"
        );

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MaintenanceLogSnapshotEvent>
    maintenanceLogSnapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MaintenanceLogSnapshotEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(maintenanceLogSnapshotConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, MaintenanceItemStandardSnapshotEvent> maintenanceItemStandardSnapshotConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-maintenance-item");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<MaintenanceItemStandardSnapshotEvent> deserializer =
            new JsonDeserializer<>(MaintenanceItemStandardSnapshotEvent.class);
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages(
            "com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto",
            "com.ohgiraffers.team3backendadmin.infrastructure.kafka.dto"
        );

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MaintenanceItemStandardSnapshotEvent>
    maintenanceItemStandardSnapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MaintenanceItemStandardSnapshotEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(maintenanceItemStandardSnapshotConsumerFactory());
        return factory;
    }
}
