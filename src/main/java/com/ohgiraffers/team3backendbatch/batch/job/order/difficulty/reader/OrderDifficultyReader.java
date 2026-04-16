package com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultySource;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderRegisteredEvent;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class OrderDifficultyReader implements ItemReader<OrderDifficultySource> {

    private static final Logger log = LoggerFactory.getLogger(OrderDifficultyReader.class);

    private final ObjectMapper objectMapper;
    private final String orderEventPayload;
    private final String analysisReferenceDate;
    private boolean consumed;

    /**
     * 주문 난이도 분석 Reader 를 생성한다.
     * @param objectMapper JSON 역직렬화 객체
     * @param orderEventPayload 주문 등록 이벤트 payload
     * @param analysisReferenceDate 분석 기준일 문자열
     */
    public OrderDifficultyReader(
        ObjectMapper objectMapper,
        @Value("#{jobParameters['orderEventPayload']}") String orderEventPayload,
        @Value("#{jobParameters['analysisReferenceDate']}") String analysisReferenceDate
    ) {
        this.objectMapper = objectMapper;
        this.orderEventPayload = orderEventPayload;
        this.analysisReferenceDate = analysisReferenceDate;
    }

    /**
     * 주문 난이도 분석 원천 데이터를 한 건 반환한다.
     * @param 없음
     * @return 주문 난이도 분석 원천 데이터
     */
    @Override
    public OrderDifficultySource read() {
        if (consumed) {
            return null;
        }
        consumed = true;

        if (orderEventPayload == null || orderEventPayload.isBlank()) {
            throw new IllegalStateException("orderEventPayload job parameter is required for order difficulty analysis.");
        }

        OrderRegisteredEvent event = deserializePayload();
        LocalDate referenceDate = event.getOccurredAt() == null
            ? parseReferenceDate(analysisReferenceDate)
            : event.getOccurredAt().toLocalDate();
        OrderDifficultySource source = toSource(event, referenceDate);

        log.info(
            "Loaded order difficulty source from event payload. orderId={}, configId={}, productId={}, referenceDate={}, dueDate={}, quantity={}",
            source.getOrderId(),
            source.getConfigId(),
            source.getProductId(),
            source.getReferenceDate(),
            source.getDueDate(),
            source.getOrderQuantity()
        );
        return source;
    }

    /**
     * 주문 등록 이벤트 payload 를 역직렬화한다.
     * @param 없음
     * @return 주문 등록 이벤트
     */
    private OrderRegisteredEvent deserializePayload() {
        try {
            return objectMapper.readValue(orderEventPayload, OrderRegisteredEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize order registered event payload.", exception);
        }
    }

    /**
     * 주문 등록 이벤트를 난이도 분석 원천 객체로 변환한다.
     * @param event 주문 등록 이벤트
     * @param referenceDate 분석 기준일
     * @return 주문 난이도 분석 원천 객체
     */
    private OrderDifficultySource toSource(OrderRegisteredEvent event, LocalDate referenceDate) {
        OrderDifficultySource source = new OrderDifficultySource();
        source.setOrderId(event.getOrderId());
        source.setProductId(event.getProductId());
        source.setConfigId(event.getConfigId());
        source.setOrderNumber(event.getOrderNumber());
        source.setOrderQuantity(event.getOrderQuantity());
        source.setProcessStepCount(event.getProcessStepCount());
        source.setToleranceMm(event.getToleranceMm());
        source.setSkillLevel(event.getSkillLevel());
        source.setReferenceDate(referenceDate);
        source.setDueDate(event.getDueDate());
        source.setFirstOrder(event.getFirstOrder());
        source.setIndustryPreset(event.getIndustryPreset());
        source.setWeightV1(event.getWeightV1());
        source.setWeightV2(event.getWeightV2());
        source.setWeightV3(event.getWeightV3());
        source.setWeightV4(event.getWeightV4());
        source.setAlphaWeight(event.getAlphaWeight());
        source.setProductName(event.getProductName());
        source.setProductCode(event.getProductCode());
        return source;
    }

    /**
     * 분석 기준일 문자열을 LocalDate 로 변환한다.
     * @param value 분석 기준일 문자열
     * @return 변환된 기준일
     */
    private LocalDate parseReferenceDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (RuntimeException exception) {
            log.warn("Invalid analysisReferenceDate job parameter. value={}", value, exception);
            return null;
        }
    }
}
