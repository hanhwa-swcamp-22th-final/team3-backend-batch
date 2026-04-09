package com.ohgiraffers.team3backendbatch.infrastructure.kafka.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohgiraffers.team3backendbatch.api.command.dto.BatchJobLaunchRequest;
import com.ohgiraffers.team3backendbatch.batch.common.launcher.BatchJobLauncherFacade;
import com.ohgiraffers.team3backendbatch.batch.common.support.BatchJobNames;
import com.ohgiraffers.team3backendbatch.infrastructure.kafka.dto.OrderRegisteredEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderRegisteredListenerTest {

    @Mock
    private BatchJobLauncherFacade batchJobLauncherFacade;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderRegisteredListener orderRegisteredListener;

    @Test
    @DisplayName("Launches order difficulty batch job when order registered event is received")
    void listen_LaunchesBatchJob() throws Exception {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 1, 10, 30);
        OrderRegisteredEvent event = new OrderRegisteredEvent(
            77L,
            10L,
            20L,
            "ORD-077",
            120,
            LocalDate.of(2026, 4, 6),
            true,
            occurredAt
        );
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"orderId\":77}");

        orderRegisteredListener.listen(event);

        ArgumentCaptor<BatchJobLaunchRequest> requestCaptor = ArgumentCaptor.forClass(BatchJobLaunchRequest.class);
        verify(batchJobLauncherFacade).launch(eq(BatchJobNames.ORDER_DIFFICULTY_ANALYSIS_JOB), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getOrderId()).isEqualTo(77L);
        assertThat(requestCaptor.getValue().getAnalysisReferenceDate()).isEqualTo(occurredAt.toLocalDate());
        assertThat(requestCaptor.getValue().getOrderEventPayload()).isNotBlank();
    }
}
