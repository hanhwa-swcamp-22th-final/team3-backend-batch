package com.ohgiraffers.team3backendbatch.infrastructure.kafka.config;

import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesEnvironmentEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesEquipmentStatusEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesProductionResultEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesQualityMeasurementEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.MesQualityResultEvent;
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
public class MesKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${batch.mes.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Bean
    public ConsumerFactory<String, MesProductionResultEvent> mesProductionResultConsumerFactory() {
        return consumerFactory(MesProductionResultEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MesProductionResultEvent>
    mesProductionResultKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MesProductionResultEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mesProductionResultConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, MesQualityResultEvent> mesQualityResultConsumerFactory() {
        return consumerFactory(MesQualityResultEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MesQualityResultEvent>
    mesQualityResultKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MesQualityResultEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mesQualityResultConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, MesQualityMeasurementEvent> mesQualityMeasurementConsumerFactory() {
        return consumerFactory(MesQualityMeasurementEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MesQualityMeasurementEvent>
    mesQualityMeasurementKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MesQualityMeasurementEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mesQualityMeasurementConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, MesEquipmentStatusEvent> mesEquipmentStatusConsumerFactory() {
        return consumerFactory(MesEquipmentStatusEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MesEquipmentStatusEvent>
    mesEquipmentStatusKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MesEquipmentStatusEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mesEquipmentStatusConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, MesEnvironmentEvent> mesEnvironmentConsumerFactory() {
        return consumerFactory(MesEnvironmentEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MesEnvironmentEvent>
    mesEnvironmentKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MesEnvironmentEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mesEnvironmentConsumerFactory());
        return factory;
    }

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> targetType) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType);
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages("com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }
}
