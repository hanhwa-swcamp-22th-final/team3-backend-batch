package com.ohgiraffers.team3backendbatch.infrastructure.kafka.config;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.AssignmentSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderDifficultyAnalyzedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderDifficultySnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderRegisteredEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
public class OrderDifficultyKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ProducerFactory<String, OrderDifficultyAnalyzedEvent> orderDifficultyAnalyzedProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, OrderDifficultyAnalyzedEvent> orderDifficultyAnalyzedKafkaTemplate() {
        return new KafkaTemplate<>(orderDifficultyAnalyzedProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, OrderRegisteredEvent> orderRegisteredConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-order");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<OrderRegisteredEvent> deserializer = new JsonDeserializer<>(OrderRegisteredEvent.class);
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages(
            "com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto",
            "com.ohgiraffers.team3backendscm.infrastructure.kafka.dto"
        );

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderRegisteredEvent>
    orderRegisteredKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderRegisteredEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderRegisteredConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderDifficultySnapshotEvent> orderDifficultySnapshotConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-order-ref");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<OrderDifficultySnapshotEvent> deserializer =
            new JsonDeserializer<>(OrderDifficultySnapshotEvent.class);
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages(
            "com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto",
            "com.ohgiraffers.team3backendscm.infrastructure.kafka.dto"
        );

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderDifficultySnapshotEvent>
    orderDifficultySnapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderDifficultySnapshotEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderDifficultySnapshotConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, AssignmentSnapshotEvent> assignmentSnapshotConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-assignment-ref");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<AssignmentSnapshotEvent> deserializer =
            new JsonDeserializer<>(AssignmentSnapshotEvent.class);
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages(
            "com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto",
            "com.ohgiraffers.team3backendscm.infrastructure.kafka.dto"
        );

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AssignmentSnapshotEvent>
    assignmentSnapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AssignmentSnapshotEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(assignmentSnapshotConsumerFactory());
        return factory;
    }
}
