package eventsourced.query.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import events.OrderConfirmed;
import events.OrderCreated;
import eventsourced.query.dto.OrderView;
import eventsourced.query.readmodel.OrderReadModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryConsumerServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private OrderReadModel readModel;

    private QueryConsumerService consumerService;

    @BeforeEach
    void setUp() {
        consumerService = new QueryConsumerService(objectMapper, readModel, new SimpleMeterRegistry());
    }

    @Test
    void handleMessage_orderCreatedJson_putsOrderViewInReadModel() throws Exception {
        OrderCreated event = new OrderCreated(
                "order-1",
                1L,
                "customer-1",
                "50.00",
                Instant.parse("2025-02-16T10:00:00Z")
        );
        String json = objectMapper.writeValueAsString((events.OrderEvent) event);

        consumerService.handleMessage(json);

        ArgumentCaptor<OrderView> viewCaptor = ArgumentCaptor.forClass(OrderView.class);
        verify(readModel).put(viewCaptor.capture());
        OrderView view = viewCaptor.getValue();
        assertThat(view.orderId()).isEqualTo("order-1");
        assertThat(view.customerId()).isEqualTo("customer-1");
        assertThat(view.amount()).isEqualTo("50.00");
        assertThat(view.createdAt()).isEqualTo(Instant.parse("2025-02-16T10:00:00Z"));
        assertThat(view.confirmed()).isFalse();
        assertThat(view.products()).isNull();
        assertThat(view.confirmedAt()).isNull();
    }

    @Test
    void handleMessage_orderConfirmedJson_updatesExistingOrderInReadModel() throws Exception {
        OrderView existing = OrderView.fromCreated(
                "order-2",
                "customer-2",
                "75.00",
                Instant.parse("2025-02-16T09:00:00Z")
        );
        when(readModel.get("order-2")).thenReturn(java.util.Optional.of(existing));

        OrderConfirmed event = new OrderConfirmed(
                "order-2",
                2L,
                "customer-2",
                List.of("item-A", "item-B"),
                Instant.parse("2025-02-16T11:00:00Z")
        );
        String json = objectMapper.writeValueAsString((events.OrderEvent) event);

        consumerService.handleMessage(json);

        ArgumentCaptor<OrderView> viewCaptor = ArgumentCaptor.forClass(OrderView.class);
        verify(readModel).put(viewCaptor.capture());
        OrderView updated = viewCaptor.getValue();
        assertThat(updated.orderId()).isEqualTo("order-2");
        assertThat(updated.customerId()).isEqualTo("customer-2");
        assertThat(updated.amount()).isEqualTo("75.00");
        assertThat(updated.confirmed()).isTrue();
        assertThat(updated.products()).containsExactly("item-A", "item-B");
        assertThat(updated.confirmedAt()).isEqualTo(Instant.parse("2025-02-16T11:00:00Z"));
    }
}
