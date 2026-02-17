package eventsourced.command.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import events.Topics;
import eventsourced.command.dto.ConfirmOrderCommand;
import eventsourced.command.dto.CreateOrderCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommandServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private CommandService commandService;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        commandService = new CommandService(mapper, kafkaTemplate, new SimpleMeterRegistry());
    }

    @Test
    void createOrder_returnsOrderId_andSendsOrderCreatedToKafka() {
        CreateOrderCommand command = new CreateOrderCommand("customer-1", "99.99");

        String orderId = commandService.createOrder(command);

        assertThat(orderId).isNotBlank();

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(Topics.ORDER_EVENTS);
        assertThat(keyCaptor.getValue()).isEqualTo(orderId);
        assertThat(valueCaptor.getValue()).contains("\"eventType\":\"OrderCreated\"")
                .contains("\"customerId\":\"customer-1\"")
                .contains("\"amount\":\"99.99\"")
                .contains("\"sequenceNumber\":1");
    }

    @Test
    void confirmOrder_sendsOrderConfirmedToKafka() {
        String orderId = "order-123";
        ConfirmOrderCommand command = new ConfirmOrderCommand(orderId, List.of("item-A", "item-B"));

        commandService.confirmOrder(orderId, command);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(Topics.ORDER_EVENTS);
        assertThat(keyCaptor.getValue()).isEqualTo(orderId);
        assertThat(valueCaptor.getValue()).contains("\"eventType\":\"OrderConfirmed\"")
                .contains("\"orderId\":\"order-123\"")
                .contains("\"sequenceNumber\":2")
                .contains("item-A")
                .contains("item-B");
    }
}
