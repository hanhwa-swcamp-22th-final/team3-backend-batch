package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchJobLaunchRequest;
import com.ohgiraffers.team3backendbatch.api.command.dto.ManualJobLaunchMode;
import com.ohgiraffers.team3backendbatch.batch.common.launcher.BatchJobLauncherFacade;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderRegisteredEvent;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.support.OrderDifficultyKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(OrderRegisteredListener.class);

    private final BatchJobLauncherFacade batchJobLauncherFacade;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = OrderDifficultyKafkaTopics.ORDER_REGISTERED,
        containerFactory = "orderRegisteredKafkaListenerContainerFactory"
    )
    public void listen(OrderRegisteredEvent event) {
        log.info(
            "Received order registered event. orderId={}, productId={}, configId={}, quantity={}, dueDate={}, occurredAt={}",
            event.getOrderId(),
            event.getProductId(),
            event.getConfigId(),
            event.getOrderQuantity(),
            event.getDueDate(),
            event.getOccurredAt()
        );

        batchJobLauncherFacade.launch(
            BatchJobNames.ORDER_DIFFICULTY_ANALYSIS_JOB,
            BatchJobLaunchRequest.builder()
                .mode(ManualJobLaunchMode.ORDER)
                .orderId(event.getOrderId())
                .analysisReferenceDate(event.getOccurredAt() == null ? null : event.getOccurredAt().toLocalDate())
                .orderEventPayload(toPayload(event))
                .force(Boolean.TRUE)
                .requestedBy("scm-kafka")
                .reason("Order registered event")
                .build()
        );
    }

    private String toPayload(OrderRegisteredEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize order registered event payload.", exception);
        }
    }
}
