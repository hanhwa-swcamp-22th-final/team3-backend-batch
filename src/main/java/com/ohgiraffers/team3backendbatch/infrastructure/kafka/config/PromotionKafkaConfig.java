package com.ohgiraffers.team3backendbatch.infrastructure.kafka.config;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.EvaluationWeightConfigSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointCalculatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PerformancePointSnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MissionProgressEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PromotionCandidateEvaluatedEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.PromotionHistorySnapshotEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.TierConfigSnapshotEvent;
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
public class PromotionKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ProducerFactory<String, PerformancePointCalculatedEvent> performancePointCalculatedProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, PerformancePointCalculatedEvent> performancePointCalculatedKafkaTemplate() {
        return new KafkaTemplate<>(performancePointCalculatedProducerFactory());
    }

    @Bean
    public ProducerFactory<String, PromotionCandidateEvaluatedEvent> promotionCandidateEvaluatedProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, PromotionCandidateEvaluatedEvent> promotionCandidateEvaluatedKafkaTemplate() {
        return new KafkaTemplate<>(promotionCandidateEvaluatedProducerFactory());
    }

    @Bean
    public ProducerFactory<String, MissionProgressEvent> missionProgressProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, MissionProgressEvent> missionProgressKafkaTemplate() {
        return new KafkaTemplate<>(missionProgressProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, PerformancePointSnapshotEvent> performancePointSnapshotConsumerFactory() {
        JsonDeserializer<PerformancePointSnapshotEvent> deserializer =
            new JsonDeserializer<>(PerformancePointSnapshotEvent.class);
        configureDeserializer(deserializer);
        return new DefaultKafkaConsumerFactory<>(consumerConfig(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PerformancePointSnapshotEvent>
    performancePointSnapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PerformancePointSnapshotEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(performancePointSnapshotConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PromotionHistorySnapshotEvent> promotionHistorySnapshotConsumerFactory() {
        JsonDeserializer<PromotionHistorySnapshotEvent> deserializer =
            new JsonDeserializer<>(PromotionHistorySnapshotEvent.class);
        configureDeserializer(deserializer);
        return new DefaultKafkaConsumerFactory<>(consumerConfig(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PromotionHistorySnapshotEvent>
    promotionHistorySnapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PromotionHistorySnapshotEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(promotionHistorySnapshotConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, TierConfigSnapshotEvent> tierConfigSnapshotConsumerFactory() {
        JsonDeserializer<TierConfigSnapshotEvent> deserializer =
            new JsonDeserializer<>(TierConfigSnapshotEvent.class);
        configureDeserializer(deserializer);
        return new DefaultKafkaConsumerFactory<>(consumerConfig(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TierConfigSnapshotEvent>
    tierConfigSnapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TierConfigSnapshotEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(tierConfigSnapshotConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, EvaluationWeightConfigSnapshotEvent> evaluationWeightConfigSnapshotConsumerFactory() {
        JsonDeserializer<EvaluationWeightConfigSnapshotEvent> deserializer =
            new JsonDeserializer<>(EvaluationWeightConfigSnapshotEvent.class);
        configureDeserializer(deserializer);
        return new DefaultKafkaConsumerFactory<>(consumerConfig(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EvaluationWeightConfigSnapshotEvent>
    evaluationWeightConfigSnapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EvaluationWeightConfigSnapshotEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(evaluationWeightConfigSnapshotConsumerFactory());
        return factory;
    }

    private Map<String, Object> producerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return config;
    }

    private Map<String, Object> consumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

    private void configureDeserializer(JsonDeserializer<?> deserializer) {
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages(
            "com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto",
            "com.ohgiraffers.team3backendhr.infrastructure.kafka.dto"
        );
    }
}
