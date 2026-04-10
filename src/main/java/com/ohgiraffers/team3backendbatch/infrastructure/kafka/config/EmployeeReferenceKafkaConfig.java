package com.ohgiraffers.team3backendbatch.infrastructure.kafka.config;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EmployeeSnapshotEvent;
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
public class EmployeeReferenceKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, EmployeeSnapshotEvent> employeeSnapshotConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<EmployeeSnapshotEvent> deserializer = new JsonDeserializer<>(EmployeeSnapshotEvent.class);
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages(
            "com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto",
            "com.ohgiraffers.team3backendadmin.infrastructure.kafka.dto"
        );

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmployeeSnapshotEvent>
    employeeSnapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EmployeeSnapshotEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(employeeSnapshotConsumerFactory());
        return factory;
    }
}
