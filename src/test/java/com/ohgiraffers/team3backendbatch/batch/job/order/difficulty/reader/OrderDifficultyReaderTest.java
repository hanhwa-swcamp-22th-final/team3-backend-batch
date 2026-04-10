package com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.reader;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.batch.job.order.difficulty.model.OrderDifficultySource;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderRegisteredEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderDifficultyReaderTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @DisplayName("Reads order difficulty source from registered event payload")
    void read_LoadsSourceFromEventPayload() throws Exception {
        OrderRegisteredEvent event = new OrderRegisteredEvent(
            77L,
            10L,
            20L,
            "ORD-077",
            120,
            18,
            new BigDecimal("0.0100"),
            4,
            LocalDate.of(2026, 4, 6),
            true,
            "PRECISION SENSOR MODULE",
            "TI-6AL-4V-CMM",
            "SEMICONDUCTOR",
            new BigDecimal("0.20"),
            new BigDecimal("0.40"),
            new BigDecimal("0.25"),
            new BigDecimal("0.15"),
            new BigDecimal("0.50"),
            LocalDateTime.of(2026, 4, 1, 10, 30)
        );
        OrderDifficultyReader reader = new OrderDifficultyReader(
            objectMapper,
            objectMapper.writeValueAsString(event),
            null
        );

        OrderDifficultySource source = reader.read();

        assertThat(source).isNotNull();
        assertThat(source.getOrderId()).isEqualTo(77L);
        assertThat(source.getProcessStepCount()).isEqualTo(18);
        assertThat(source.getToleranceMm()).isEqualByComparingTo("0.0100");
        assertThat(source.getSkillLevel()).isEqualTo(4);
        assertThat(source.getProductName()).isEqualTo("PRECISION SENSOR MODULE");
        assertThat(source.getProductCode()).isEqualTo("TI-6AL-4V-CMM");
        assertThat(source.getWeightV2()).isEqualByComparingTo("0.40");
        assertThat(source.getReferenceDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(reader.read()).isNull();
    }
}
